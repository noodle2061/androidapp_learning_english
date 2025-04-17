package com.example.learning_english.network;

import com.android.volley.VolleyError;
import org.json.JSONObject;

/**
 * Interface callback cho các phản hồi từ ApiClient.
 */
public interface ApiResponseListener {
    /**
     * Được gọi khi yêu cầu API thành công.
     * @param response Đối tượng JSON chứa phản hồi từ API.
     */
    void onSuccess(JSONObject response);

    /**
     * Được gọi khi yêu cầu API gặp lỗi.
     * @param error Đối tượng VolleyError chứa thông tin lỗi.
     */
    void onError(VolleyError error);
}
