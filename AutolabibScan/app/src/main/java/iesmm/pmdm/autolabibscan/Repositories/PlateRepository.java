package iesmm.pmdm.autolabibscan.Repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import iesmm.pmdm.autolabibscan.Models.ApiResponse;
import iesmm.pmdm.autolabibscan.Remote.ApiClient;
import iesmm.pmdm.autolabibscan.Remote.ApiService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;

public class PlateRepository {
    private ApiService apiService;

    public PlateRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<ApiResponse> uploadImage(File file) {
        MutableLiveData<ApiResponse> data = new MutableLiveData<>();
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        apiService.uploadImage(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }
}
