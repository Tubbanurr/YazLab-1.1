package org.example.k;

public class Ingredient {
    private int id;                  // Malzeme kimliği
    private String name;             // Malzemenin adı
    private float totalQuantity;       // Toplam miktar
    private String unit;             // Miktar birimi
    private double amount;           // Kullanılacak miktar
    private double unitPrice;        // Birim fiyat

    // Constructor
    public Ingredient(String name, double amount, double unitPrice) {
        this.name = name;
        this.amount = amount;
        this.unitPrice = unitPrice;
    }

    public Ingredient(int id, String name, float totalQuantity, String unit, double unitPrice) {
        this.id = id;
        this.name = name;
        this.totalQuantity = totalQuantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
    }

    // Getters ve Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public float getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; } // Düzeltildi

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

}
