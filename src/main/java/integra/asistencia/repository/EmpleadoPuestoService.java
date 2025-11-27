package integra.asistencia.repository;

public interface EmpleadoPuestoService {
    boolean esEmpleadoNocturno(Integer idPuestoEmpleado);

    boolean tieneJornadaActivaNocturna(Integer empleadoId);
}
