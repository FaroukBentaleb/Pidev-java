package tn.learniverse.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import tn.learniverse.entities.Offre;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class QRCodeService {
    
    private static final int QR_CODE_SIZE = 300;

    public String generateQRCode(Offre offre) throws WriterException, IOException {
        // Create QR code content with offer details
        String qrContent = String.format(
            "Offer: %s\nPrice: $%.2f/month\nDiscount: %.0f%%\nValid From: %s\nValid Until: %s",
            offre.getName(),
            offre.getPricePerMonth(),
            offre.getDiscount() != null ? offre.getDiscount() : 0,
            offre.getValidFrom().toString(),
            offre.getValidUntil().toString()
        );

        // Create QR code writer
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            qrContent,
            BarcodeFormat.QR_CODE,
            QR_CODE_SIZE,
            QR_CODE_SIZE
        );

        // Convert to image
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        // Convert to Base64
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public String generateQRCode(String content) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            content,
            BarcodeFormat.QR_CODE,
            QR_CODE_SIZE,
            QR_CODE_SIZE
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
} 