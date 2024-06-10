package iesmm.pmdm.autolabibscan.Models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("matricula_detectada")
    private String matriculaDetectada;

    @SerializedName("error")
    private String error;

    public String getMatriculaDetectada() {
        return matriculaDetectada;
    }

    public void setMatriculaDetectada(String matriculaDetectada) {
        this.matriculaDetectada = matriculaDetectada;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
