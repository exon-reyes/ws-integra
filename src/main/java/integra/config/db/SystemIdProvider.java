package integra.config.db;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class SystemIdProvider {

    private final Environment env;

    public Integer getIdPuestoNocturno() {
        return env.getProperty("app.config.idPuestoNocturno", Integer.class, 2);
    }

    public LocalTime getHoraInicioNocturno() {
        String horaStr = env.getProperty("app.config.horaInicioNocturno", "18:00");
        return LocalTime.parse(horaStr);
    }

    public String getDefaultRolUsuarioNuevo() {
        return env.getProperty("app.config.defaultRolUsuarioNuevo", "Vendedor");
    }

    public Integer getIdPuestoSupervisor() {
        return env.getProperty("app.config.idPuestoSupervisor", Integer.class, 4);
    }

    public Long getIdUsuarioAdmin() {
        return env.getProperty("app.config.idUsuarioAdmin", Long.class, 1L);
    }
}