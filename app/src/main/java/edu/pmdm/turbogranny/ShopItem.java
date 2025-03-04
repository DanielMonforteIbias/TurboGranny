package edu.pmdm.turbogranny;

public class ShopItem {

    private final int id;
    private final String nombre;
    private final int precio;
    private boolean comprado;
    private final int imagenRes;


    public ShopItem(int id, int imagenRes, String nombre, int precio, boolean comprado) {
        this.id = id;
        this.imagenRes = imagenRes;
        this.nombre = nombre;
        this.precio = precio;
        this.comprado = comprado;
    }


    public ShopItem(int imagenRes, String nombre, int precio, boolean comprado) {
        this(imagenRes, imagenRes, nombre, precio, comprado);
    }


    public ShopItem(int imagenRes, String nombre, int precio) {
        this(imagenRes, imagenRes, nombre, precio, false);
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPrecio() {
        return precio;
    }

    public boolean isComprado() {
        return comprado;
    }

    public void setComprado(boolean comprado) {
        this.comprado = comprado;
    }

    public int getImagenRes() {
        return imagenRes;
    }
}
