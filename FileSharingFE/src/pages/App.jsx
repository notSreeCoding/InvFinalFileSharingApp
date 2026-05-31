import { useEffect, useMemo, useState } from "react";
import * as Dialog from "@radix-ui/react-dialog";
import {
  Check,
  Clipboard,
  Clock,
  ExternalLink,
  FileUp,
  Link2,
  LogOut,
  Mail,
  RefreshCcw,
  Share2,
  ShieldCheck,
  X,
} from "lucide-react";
import {
  clearSession,
  createShare,
  getSession,
  listFiles,
  listShares,
  login,
  logout,
  register,
  uploadFile,
} from "../api.js";

const PAGE_SIZE = 5;

export default function App() {
  const [session, setSession] = useState(() => getSession());

  const [filesPage, setFilesPage] = useState(null);
  const [sharesPage, setSharesPage] = useState(null);

  const [filePage, setFilePage] = useState(0);
  const [sharePage, setSharePage] = useState(0);

  const [selectedFile, setSelectedFile] = useState(null);
  const [pendingFile, setPendingFile] = useState(null);
  const [isDraggingFile, setIsDraggingFile] = useState(false);
  const [loading, setLoading] = useState(false);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");

  const files = filesPage?.content || [];
  const shares = sharesPage?.content || [];

  const latestSharesByFile = useMemo(() => {
    return shares.reduce((map, share) => {
      map[share.fileId] = [...(map[share.fileId] || []), share];
      return map;
    }, {});
  }, [shares]);

  async function loadFiles(page = filePage) {
    const data = await listFiles(page, PAGE_SIZE);
    setFilesPage(data);
  }

  async function loadShares(page = sharePage) {
    const data = await listShares(page, PAGE_SIZE);
    setSharesPage(data);
  }

  async function refresh() {
    if (!session.accessToken) return;

    setLoading(true);
    setError("");

    try {
      await Promise.all([loadFiles(filePage), loadShares(sharePage)]);
    } catch (err) {
      setError(apiMessage(err));
      if (err?.response?.status === 401) {
        clearSession();
        setSession(getSession());
      }
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!session.accessToken) return;
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session.accessToken, filePage, sharePage]);

  async function handleAuth(mode, payload) {
    setLoading(true);
    setError("");
    setNotice("");

    try {
      const authData = mode === "register" ? await register(payload) : await login(payload);
      setSession(getSession());
      setFilePage(0);
      setSharePage(0);
      setNotice(`Signed in as ${authData.user.email}.`);
    } catch (err) {
      setError(apiMessage(err));
    } finally {
      setLoading(false);
    }
  }

  async function handleLogout() {
    setLoading(true);
    try {
      await logout();
      setFilesPage(null);
      setSharesPage(null);
      setSession(getSession());
      setNotice("");
      setError("");
    } finally {
      setLoading(false);
    }
  }

  async function handleUpload(event) {
    event.preventDefault();
    if (!pendingFile) return;

    const form = event.currentTarget;
    setLoading(true);
    setError("");
    setNotice("");

    try {
      await uploadFile(pendingFile);
      setPendingFile(null);
      form.reset();
      setNotice("File uploaded.");
      setFilePage(0);
      await refresh();
    } catch (err) {
      setError(apiMessage(err));
    } finally {
      setLoading(false);
    }
  }

  function handleDrop(event) {
    event.preventDefault();
    setIsDraggingFile(false);
    const droppedFile = event.dataTransfer.files?.[0];
    if (droppedFile) setPendingFile(droppedFile);
  }

  async function handleShare(payload) {
    setLoading(true);
    setError("");
    setNotice("");

    try {
      const share = await createShare(selectedFile.id, payload);
      setNotice(`Share link created for ${share.recipientEmail}.`);
      setSelectedFile(null);
      setSharePage(0);
      await refresh();
    } catch (err) {
      setError(apiMessage(err));
    } finally {
      setLoading(false);
    }
  }

  async function copyLink(url) {
    await navigator.clipboard.writeText(url);
    setNotice("Share link copied.");
  }

  if (!session.accessToken) {
    return <AuthScreen onSubmit={handleAuth} loading={loading} error={error} />;
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <h1>File Sharing</h1>
          <p>Upload files, email unique public links, and track access.</p>
        </div>

        <div className="account-pill">
          <ShieldCheck size={18} />
          <span>{session.user?.email}</span>
          <button
            className="icon-button"
            type="button"
            onClick={handleLogout}
            disabled={loading}
            aria-label="Log out"
          >
            <LogOut size={18} />
          </button>
        </div>
      </header>

      <div className="toolbar">
        <form className="upload-form" onSubmit={handleUpload}>
          <label
            className={`file-picker${isDraggingFile ? " dragging" : ""}`}
            onDragEnter={(event) => {
              event.preventDefault();
              setIsDraggingFile(true);
            }}
            onDragOver={(event) => {
              event.preventDefault();
              event.dataTransfer.dropEffect = "copy";
            }}
            onDragLeave={(event) => {
              event.preventDefault();
              if (!event.currentTarget.contains(event.relatedTarget)) {
                setIsDraggingFile(false);
              }
            }}
            onDrop={handleDrop}
          >
            <FileUp size={18} />
            <span>{pendingFile ? pendingFile.name : "Choose or drop file"}</span>
            <input
              type="file"
              onChange={(event) => setPendingFile(event.target.files?.[0] || null)}
            />
          </label>

          <button className="primary-button" type="submit" disabled={loading || !pendingFile}>
            <FileUp size={18} />
            Upload
          </button>
        </form>

        <button
          className="icon-button"
          type="button"
          onClick={() => refresh()}
          disabled={loading}
          aria-label="Refresh"
        >
          <RefreshCcw size={18} />
        </button>
      </div>

      {notice && <div className="notice success">{notice}</div>}
      {error && <div className="notice error">{error}</div>}

      <div className="content-grid">
        <section className="panel">
          <div className="panel-heading">
            <h2>Your Files</h2>
            <span>{filesPage?.page?.totalElements ?? 0}</span>
          </div>

          <div className="file-list">
            {files.map((file) => (
              <div className="file-row" key={file.id}>
                <div className="file-main">
                  <div className="file-icon">
                    <FileUp size={20} />
                  </div>
                  <div>
                    <h3>{file.fileName}</h3>
                    <p>
                      {formatBytes(file.size)} · uploaded {formatDate(file.uploadedAt)}
                    </p>
                  </div>
                </div>

                <div className="file-actions">
                  <span>{latestSharesByFile[file.id]?.length || 0} shares</span>
                  <button
                    className="secondary-button"
                    type="button"
                    onClick={() => setSelectedFile(file)}
                  >
                    <Share2 size={16} />
                    Share
                  </button>
                </div>
              </div>
            ))}

            {!files.length && <EmptyState label="No files uploaded yet." />}
          </div>

          <Pagination
            pageInfo={filesPage?.page}
            onPrev={() => setFilePage((p) => Math.max(0, p - 1))}
            onNext={() =>
              setFilePage((p) =>
                filesPage?.page?.totalPages && p + 1 < filesPage.page.totalPages ? p + 1 : p,
              )
            }
            loading={loading}
          />
        </section>

        <section className="panel">
          <div className="panel-heading">
            <h2>Shared File List</h2>
            <span>{sharesPage?.page?.totalElements ?? 0}</span>
          </div>

          <div className="share-list">
            {shares.map((share) => (
              <div className="share-row" key={share.id}>
                <div>
                  <h3>{share.fileName}</h3>
                  <p>
                    <Mail size={14} />
                    {share.recipientEmail}
                  </p>
                  <p>
                    <Clock size={14} />
                    Shared {formatDate(share.shareDate)} · Expires {formatDate(share.expiresAt)}
                  </p>
                </div>

                <div className="share-status">
                  <span className={`status ${share.accessed ? "accessed" : "waiting"}`}>
                    {share.accessed ? <Check size={14} /> : <Clock size={14} />}
                    {share.accessed ? "Accessed" : "Not accessed"}
                  </span>

                  <button
                    className="icon-button"
                    type="button"
                    onClick={() => copyLink(share.shareUrl)}
                    aria-label="Copy share link"
                  >
                    <Clipboard size={16} />
                  </button>

                  <a
                    className="icon-button"
                    href={share.shareUrl}
                    target="_blank"
                    rel="noreferrer"
                    aria-label="Open share link"
                  >
                    <ExternalLink size={16} />
                  </a>
                </div>
              </div>
            ))}

            {!shares.length && <EmptyState label="No shares yet." />}
          </div>

          <Pagination
            pageInfo={sharesPage?.page}
            onPrev={() => setSharePage((p) => Math.max(0, p - 1))}
            onNext={() =>
              setSharePage((p) =>
                sharesPage?.page?.totalPages && p + 1 < sharesPage.page.totalPages ? p + 1 : p,
              )
            }
            loading={loading}
          />
        </section>
      </div>

      <ShareDialog
        file={selectedFile}
        onClose={() => setSelectedFile(null)}
        onShare={handleShare}
        loading={loading}
      />
    </main>
  );
}

function Pagination({ pageInfo, onPrev, onNext, loading }) {
  const currentPage = (pageInfo?.number ?? 0) + 1;
  const totalPages = pageInfo?.totalPages ?? 1;
  const canPrev = (pageInfo?.number ?? 0) > 0;
  const canNext = (pageInfo?.number ?? 0) + 1 < totalPages;

  return (
    <div className="pagination-bar">
      <button className="secondary-button" type="button" onClick={onPrev} disabled={!canPrev || loading}>
        Previous
      </button>

      <span className="pagination-text">
        Page {currentPage} of {totalPages}
      </span>

      <button className="secondary-button" type="button" onClick={onNext} disabled={!canNext || loading}>
        Next
      </button>
    </div>
  );
}

function AuthScreen({ onSubmit, loading, error }) {
  const [mode, setMode] = useState("login");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  function submit(event) {
    event.preventDefault();
    onSubmit(mode, mode === "register" ? { name, email, password } : { email, password });
  }

  return (
    <main className="auth-shell">
      <section className="auth-panel">
        <div className="auth-heading">
          <ShieldCheck size={26} />
          <h1>{mode === "register" ? "Create account" : "Sign in"}</h1>
        </div>

        <div className="segmented-control">
          <button
            type="button"
            className={mode === "login" ? "active" : ""}
            onClick={() => setMode("login")}
          >
            Login
          </button>
          <button
            type="button"
            className={mode === "register" ? "active" : ""}
            onClick={() => setMode("register")}
          >
            Register
          </button>
        </div>

        {error && <div className="notice error">{error}</div>}

        <form className="dialog-form" onSubmit={submit}>
          {mode === "register" && (
            <label className="user-field">
              Name
              <input
                type="text"
                value={name}
                onChange={(event) => setName(event.target.value)}
                required
              />
            </label>
          )}

          <label className="user-field">
            Email
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>

          <label className="user-field">
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>

          <button className="primary-button" type="submit" disabled={loading}>
            <ShieldCheck size={18} />
            {mode === "register" ? "Register" : "Login"}
          </button>
        </form>
      </section>
    </main>
  );
}

function ShareDialog({ file, onClose, onShare, loading }) {
  const [recipientEmail, setRecipientEmail] = useState("");
  const [expirationHours, setExpirationHours] = useState(24);
  const [message, setMessage] = useState(
    "I shared a file with you. Use the link before it expires.",
  );

  useEffect(() => {
    if (file) {
      setRecipientEmail("");
      setExpirationHours(24);
      setMessage("I shared a file with you. Use the link before it expires.");
    }
  }, [file]);

  function submit(event) {
    event.preventDefault();
    onShare({ recipientEmail, expirationHours: Number(expirationHours), message });
  }

  return (
    <Dialog.Root open={Boolean(file)} onOpenChange={(open) => !open && onClose()}>
      <Dialog.Portal>
        <Dialog.Overlay className="dialog-overlay" />
        <Dialog.Content className="dialog-content">
          <div className="dialog-heading">
            <div>
              <Dialog.Title>Share file</Dialog.Title>
              <Dialog.Description>{file?.fileName}</Dialog.Description>
            </div>

            <Dialog.Close asChild>
              <button className="icon-button" type="button" aria-label="Close">
                <X size={16} />
              </button>
            </Dialog.Close>
          </div>

          <form className="dialog-form" onSubmit={submit}>
            <label>
              Recipient email
              <input
                type="email"
                value={recipientEmail}
                onChange={(event) => setRecipientEmail(event.target.value)}
                required
              />
            </label>

            <label>
              Expiration hours
              <input
                type="number"
                min="1"
                max="720"
                value={expirationHours}
                onChange={(event) => setExpirationHours(event.target.value)}
                required
              />
            </label>

            <label>
              Email message
              <textarea value={message} onChange={(event) => setMessage(event.target.value)} />
            </label>

            <button className="primary-button" type="submit" disabled={loading}>
              <Link2 size={18} />
              Create share link
            </button>
          </form>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}

function EmptyState({ label }) {
  return <div className="empty-state">{label}</div>;
}

function apiMessage(err) {
  return err?.response?.data?.message || err?.message || "Something went wrong.";
}

function formatDate(value) {
  if (!value) return "—";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return "—";

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(d);
}

function formatBytes(bytes) {
  const n = Number(bytes);
  if (!Number.isFinite(n) || n <= 0) return "0 B";

  const units = ["B", "KB", "MB", "GB"];
  const index = Math.min(Math.floor(Math.log(n) / Math.log(1024)), units.length - 1);
  return `${(n / 1024 ** index).toFixed(index === 0 ? 0 : 1)} ${units[index]}`;
}