package integra.asistencia.service;

import integra.asistencia.entity.AsistenciaModel;
import integra.asistencia.repository.AsistenciaRepository;
import integra.asistencia.repository.EmpleadoPuestoService;
import integra.asistencia.repository.PausaModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JornadaCierreScheduler {

    private final AsistenciaRepository asistenciaRepository;
    private final PausaModelRepository pausaRepository;
    private final EmpleadoPuestoService empleadoPuestoService;

    /**
     * Tarea programada para cerrar jornadas que quedaron abiertas al final del día.
     * Se ejecuta todos los días a las 23:35:50 (11:35:50 p.m.).
     * Ignora a los empleados con turno nocturno.
     */
    @Scheduled(cron = "00 50 23 * * ?")
    @Transactional
    public void cerrarJornadasAbiertas() {
        log.info("📢 Iniciando tarea programada: Cierre de jornadas abiertas (Cron: 00:18:08).");

        // Se usa el método optimizado para evitar problemas de rendimiento (N+1)
        List<AsistenciaModel> jornadasAbiertas = asistenciaRepository.findAllByJornadaCerradaFalseWithEmpleado();

        if (jornadasAbiertas.isEmpty()) {
            log.info("✅ No se encontraron jornadas abiertas. Tarea finalizada.");
            return;
        }

        log.info("🔍 Se encontraron {} jornadas abiertas para verificar.", jornadasAbiertas.size());

        for (AsistenciaModel jornada : jornadasAbiertas) {
            final Integer empleadoId = jornada.getEmpleado().getId();

            // Log de depuración al iniciar el ciclo
            log.info("Procesando jornada ID {} para el empleado {}.", jornada.getId(), empleadoId);

            // Se asume que EmpleadoModel tiene una relación con PuestoModel.
            final Integer puestoId = jornada.getEmpleado().getPuesto().getId();

            log.info("Consultando puesto ID {} para turno nocturno (Empleado {}).", puestoId, empleadoId);
            final boolean esNocturno = empleadoPuestoService.esEmpleadoNocturno(puestoId);

            if (esNocturno) {
                log.info("⏭️ Ignorando jornada ID {} (Empleado {}): Detectado turno nocturno.", jornada.getId(), empleadoId);
                continue;
            }

            log.info("⚙️ Iniciando cierre automático para jornada ID {} (Empleado {}).", jornada.getId(), empleadoId);

            // Cierra cualquier pausa activa que haya quedado abierta
            pausaRepository.findFirstByAsistencia_Empleado_IdAndFinNullOrderByInicioDesc(empleadoId)
                    .ifPresent(pausaActiva -> {
                        log.warn("🚨 Cerrando automáticamente Pausa Activa ID {} para el empleado {}. Motivo: Jornada cerrándose.", pausaActiva.getId(), empleadoId);

                        // Es crucial establecer la hora de finalización (fin)
                        pausaActiva.setFin(LocalDateTime.now());
                        pausaRepository.save(pausaActiva);
                        log.debug("Pausa activa cerrada y guardada.");
                    });

            // Finaliza la jornada y añade información de auditoría
            jornada.setFinJornada(LocalDateTime.now()); // Se establece el momento del cierre
            jornada.setJornadaCerrada(true);
            jornada.setComentario("Cierre automático por fin de día.");
            jornada.setCerradoAutomatico(true); // Se utiliza el campo de auditoría
            asistenciaRepository.save(jornada);

            log.info("💾 Jornada ID {} cerrada y actualizada en DB.", jornada.getId());
        }

        log.info("🏁 Tarea programada de cierre de jornadas finalizada.");
    }
}