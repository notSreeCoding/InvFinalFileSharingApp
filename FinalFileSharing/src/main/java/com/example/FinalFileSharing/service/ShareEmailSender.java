package com.example.FinalFileSharing.service;

import com.example.FinalFileSharing.model.FileShare;

public interface ShareEmailSender {

	void sendShareEmail(FileShare share, String shareUrl);
}
