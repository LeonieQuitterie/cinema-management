// src/main/java/com/cinema/utils/CinemaBankApiClient.java
package com.cinema.utils;

import com.cinema.models.dto.ApiResponse;
import com.cinema.models.dto.BankInfoDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CinemaBankApiClient {

    private static final String BASE_URL = "http://localhost:3000/api";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Lấy thông tin tài khoản ngân hàng của rạp
     * 
     * @param cinemaId - ID của rạp (VD: "cin_001")
     * @return BankInfoDTO hoặc null nếu có lỗi
     */
    public static BankInfoDTO getBankInfo(String cinemaId) {
        if (cinemaId == null || cinemaId.isEmpty()) {
            System.err.println("❌ Cinema ID is null or empty");
            return null;
        }

        String url = BASE_URL + "/cinema-bank/" + cinemaId + "/bank-info";

        System.out.println("🏦 Fetching bank info for cinema: " + cinemaId);
        System.out.println("🔗 API Call: GET " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("📥 Response Code: " + response.code());

            if (!response.isSuccessful()) {
                System.err.println("❌ API Error: " + response.code());
                return null;
            }

            String jsonResponse = response.body().string();
            System.out.println("📄 Response Body: " + jsonResponse);

            // Parse response
            ApiResponse<BankInfoDTO> apiResponse = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<ApiResponse<BankInfoDTO>>() {
                    });

            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                BankInfoDTO bankInfo = apiResponse.getData();
                System.out.println("  ✓ Bank: " + bankInfo.getBankName());
                System.out.println("  ✓ Account: " + bankInfo.getBankAccountNumber());
                System.out.println("  ✓ Holder: " + bankInfo.getBankAccountHolder());
                return bankInfo;
            } else {
                System.err.println("❌ API returned success=false or null data");
                return null;
            }

        } catch (IOException e) {
            System.err.println("❌ Error fetching bank info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String generateDynamicQRUrl(BankInfoDTO bankInfo, String bookingId, double amount) {
        if (bankInfo == null ||
                bankInfo.getBankAccountNumber() == null ||
                bankInfo.getBankAccountNumber().trim().isEmpty()) {
            System.err.println("❌ Bank info or account number is null/empty");
            return "https://via.placeholder.com/400x400?text=QR+Not+Available";
        }

        // 1. Lấy mã ngân hàng từ tên ngân hàng
        String bankCode = getBankCode(bankInfo.getBankName());
        if (bankCode == null) {
            System.err.println("❌ Không hỗ trợ ngân hàng: " + bankInfo.getBankName());
            return "https://via.placeholder.com/400x400?text=Bank+Not+Supported";
        }

        // 2. Encode các tham số
        String accountNumber = bankInfo.getBankAccountNumber().trim();
        String accountName = URLEncoder.encode(bankInfo.getBankAccountHolder().trim(), StandardCharsets.UTF_8);
        String addInfo = URLEncoder.encode("CINEMA " + bookingId, StandardCharsets.UTF_8); // nội dung đẹp, dễ đối soát

        // 3. Tự động generate QR động (không cần lưu template nữa)
        String qrUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%.0f&addInfo=%s&accountName=%s",
                bankCode,
                accountNumber,
                amount, // không dấu chấm, không đơn vị
                addInfo,
                accountName);

        System.out.println("🔗 Generated Dynamic QR URL: " + qrUrl);
        return qrUrl;
    }

    private static String getBankCode(String bankName) {
        if (bankName == null)
            return null;

        return switch (bankName.toLowerCase().trim()) {
            case "vietcombank", "vcb" -> "VCB";
            case "techcombank", "tcb" -> "TCB";
            case "bidv" -> "BIDV";
            case "vietinbank", "ctg" -> "CTG";
            case "mb bank", "mb", "mbbank" -> "MB";
            case "tpbank", "tpb" -> "TPB";
            case "acb" -> "ACB";
            case "sacombank", "scb" -> "SCB";
            case "vpbank" -> "VPB";
            case "agribank" -> "VBA";
            case "hdbank" -> "HDB";
            case "shb" -> "SHB";
            case "oceanbank" -> "OJB";
            // Thêm các ngân hàng khác khi cần
            default -> {
                System.err.println("⚠️ Ngân hàng chưa được hỗ trợ: " + bankName);
                yield null;
            }
        };
    }
}