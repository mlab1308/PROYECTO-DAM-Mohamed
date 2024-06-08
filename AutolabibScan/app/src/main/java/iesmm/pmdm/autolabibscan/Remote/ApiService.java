package iesmm.pmdm.autolabibscan.Remote;


import iesmm.pmdm.autolabibscan.Models.ApiResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("/reconocer_matricula/")
    Call<ApiResponse> uploadImage(@Part MultipartBody.Part file);
}
