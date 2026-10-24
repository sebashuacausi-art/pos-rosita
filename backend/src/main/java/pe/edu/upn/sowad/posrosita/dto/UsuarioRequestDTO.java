package pe.edu.upn.sowad.posrosita.dto;

// RNF-06: la clave viaja solo en el request y jamas se devuelve al frontend
public class UsuarioRequestDTO {

    private Integer idUsuario;      // ID numerico de acceso (ej. 777888)
    private String nombres;
    private String apellidoPaterno;
    private String clave;           // obligatoria al crear; opcional al editar (vacia = no cambia)
    private Integer idRol;

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
}
