package model;

public enum MsgType {
    START,   // Inicio de un cliente
    EMAIL,   // Mensaje normal
    END,     // Fin de un cliente
    FIN      // Señal de fin del sistema para servidores/cuarentena
}
