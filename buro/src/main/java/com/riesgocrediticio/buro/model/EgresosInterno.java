package com.riesgocrediticio.buro.model;

import com.riesgocrediticio.buro.enums.MoraEnum;
import com.riesgocrediticio.buro.enums.MoraTresMesesEnum;
import com.riesgocrediticio.buro.enums.ProductoInternoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "egresos", schema = "buro_interno")
public class EgresosInterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "cedula_cliente", nullable = false, length = 10)
    private String cedulaCliente;

    @Column(name = "nombres", nullable = false, length = 60)
    private String nombres;

    @Column(name = "institucion_bancaria", nullable = false, length = 35)
    private String institucionBancaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "producto", nullable = false)
    private ProductoInternoEnum producto;

    @Column(name = "saldo_pendiente", precision = 12, scale = 2)
    private BigDecimal saldoPendiente;

    @Column(name = "meses_pendientes", precision = 3)
    private Integer mesesPendientes;

    @Column(name = "cuota_pago", precision = 6, scale = 2)
    private BigDecimal cuotaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "mora", nullable = false)
    private MoraEnum mora;

    @Enumerated(EnumType.STRING)
    @Column(name = "mora_ultimos_3_meses", nullable = false)
    private MoraTresMesesEnum moraUltimosTresMeses;

    @Column(name = "fecha_actualizacion")
    private LocalDate fechaActualizacion;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @Version
    @Column(name = "version")
    private Long version;

    // Constructores
    public EgresosInterno() {
    }

    public EgresosInterno(Long id) {
        this.id = id;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCedulaCliente() {
        return cedulaCliente;
    }

    public void setCedulaCliente(String cedulaCliente) {
        this.cedulaCliente = cedulaCliente;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getInstitucionBancaria() {
        return institucionBancaria;
    }

    public void setInstitucionBancaria(String institucionBancaria) {
        this.institucionBancaria = institucionBancaria;
    }

    public ProductoInternoEnum getProducto() {
        return producto;
    }

    public void setProducto(ProductoInternoEnum producto) {
        this.producto = producto;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public Integer getMesesPendientes() {
        return mesesPendientes;
    }

    public void setMesesPendientes(Integer mesesPendientes) {
        this.mesesPendientes = mesesPendientes;
    }

    public BigDecimal getCuotaPago() {
        return cuotaPago;
    }

    public void setCuotaPago(BigDecimal cuotaPago) {
        this.cuotaPago = cuotaPago;
    }

    public MoraEnum getMora() {
        return mora;
    }

    public void setMora(MoraEnum mora) {
        this.mora = mora;
    }

    public MoraTresMesesEnum getMoraUltimosTresMeses() {
        return moraUltimosTresMeses;
    }

    public void setMoraUltimosTresMeses(MoraTresMesesEnum moraUltimosTresMeses) {
        this.moraUltimosTresMeses = moraUltimosTresMeses;
    }

    public LocalDate getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDate fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EgresosInterno that = (EgresosInterno) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EgresosInterno{" +
                "id=" + id +
                ", cedulaCliente='" + cedulaCliente + '\'' +
                ", nombres='" + nombres + '\'' +
                ", institucionBancaria='" + institucionBancaria + '\'' +
                ", producto=" + producto +
                ", saldoPendiente=" + saldoPendiente +
                ", mesesPendientes=" + mesesPendientes +
                ", cuotaPago=" + cuotaPago +
                ", mora=" + mora +
                ", moraUltimosTresMeses=" + moraUltimosTresMeses +
                ", fechaActualizacion=" + fechaActualizacion +
                ", fechaRegistro=" + fechaRegistro +
                ", version=" + version +
                '}';
    }
}