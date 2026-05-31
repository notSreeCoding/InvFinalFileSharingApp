import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { AlertCircle, Clock, Download, FileText } from "lucide-react";
import { getPublicShare } from "../api.js";

export default function PublicSharePage() {
  const { token } = useParams();
  const [share, setShare] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        setShare(await getPublicShare(token));
      } catch (err) {
        setError(err?.response?.data?.message || "This share link is unavailable.");
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [token]);

  return (
    <main className="public-shell">
      <section className="public-panel">
        {loading && <div className="empty-state">Loading shared file...</div>}
        {!loading && error && (
          <div className="public-error">
            <AlertCircle size={28} />
            <h1>Link unavailable</h1>
            <p>{error}</p>
          </div>
        )}
        {!loading && share && (
          <>
            <div className="public-file-icon">
              <FileText size={36} />
            </div>
            <h1>{share.fileName}</h1>
            <p className="public-message">{share.message}</p>
            <div className="public-meta">
              <span>{formatBytes(share.size)}</span>
              <span>
                <Clock size={14} />
                Expires {formatDate(share.expiresAt)}
              </span>
            </div>
            <a className="primary-button public-download" href={share.downloadUrl}>
              <Download size={18} />
              Download file
            </a>
          </>
        )}
      </section>
    </main>
  );
}

function formatDate(value) {
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function formatBytes(bytes) {
  if (!bytes) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  const index = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
  return `${(bytes / 1024 ** index).toFixed(index === 0 ? 0 : 1)} ${units[index]}`;
}
