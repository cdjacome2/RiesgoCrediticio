package com.riesgocrediticio.buro.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "ingresos", schema = "buro_externo")
public class IngresosExterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "cedula_cliente", nullable = false, length = 10)
    private String cedulaCliente;

    @Column(name = "nombres", nullable = false, length = 80)
    private String nombres;

    @Column(name = "institucion_bancaria", nullable = false, length = 35)
    private String institucionBancaria;

    @Column(name = "producto", length = 16)
    private String producto;

    @Column(name = "saldo_promedio_mes", precision = 12, scale = 2)
    private BigDecimal saldoPromedioMes;

    @Column(name = "numero_cuenta", length = 20)
    private String numeroCuenta;

    @Column(name = "fecha_actualizacion")
    private LocalDate fechaActualizacion;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @Version
    @Column(name = "version")
    private Long version;

    // Constructores
    public IngresosExterno() {
    }

    public IngresosExterno(Long id) {
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

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public BigDecimal getSaldoPromedioMes() {
        return saldoPromedioMes;
    }

    public void setSaldoPromedioMes(BigDecimal saldoPromedioMes) {
        this.saldoPromedioMes = saldoPromedioMes;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
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
        IngresosExterno that = (IngresosExterno) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IngresosExterno{" +
                "id=" + id +
                ", cedulaCliente='" + cedulaCliente + '\'' +
                ", nombres='" + nombres + '\'' +
                ", institucionBancaria='" + institucionBancaria + '\'' +
                ", producto='" + producto + '\'' +
                ", saldoPromedioMes=" + saldoPromedioMes +
                ", numeroCuenta='" + numeroCuenta + '\'' +
                ", fechaActualizacion=" + fechaActualizacion +
                ", fechaRegistro=" + fechaRegistro +
                ", version=" + version +
                '}';
    }
}