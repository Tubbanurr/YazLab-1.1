package org.example.k;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL = "jdbc:postgresql://localhost:5432/kilerim_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123";

    RecipeController rc ;

    // Veritabanına bağlantı kur
    public Connection connect() throws SQLException
    {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Tarif güncelleme işlemi
    public void updateRecipe(int id, String name, String category, int prepTime, String instructions, String imagePath) {
        String query = "UPDATE Tarifler SET TarifAdi = ?, Kategori = ?, HazirlamaSuresi = ?, Talimatlar = ? WHERE TarifID = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setInt(3, prepTime);
            stmt.setString(4, instructions);
            stmt.setInt(5, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Tarif güncelleme hatası...");
            e.printStackTrace();
        }
    }

    // Tarifin adını kullanarak ID'sini al
    public int getRecipeIdByName(String name) {
        String query = "SELECT TarifID FROM Tarifler WHERE TarifAdi = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("TarifID");
            }
        } catch (SQLException e) {
            System.out.println("Tarif ID alma hatası...");
            e.printStackTrace();
        }
        return -1; // Tarif yoksa -1 döndür
    }
    // Malzeme ekleme işlemi
    public void addIngredient(String name, float totalQuantity, String unit, double unitPrice) {
        String checkQuery = "SELECT MalzemeID FROM Malzemeler WHERE MalzemeAdi = ?";
        String insertQuery = "INSERT INTO Malzemeler (MalzemeAdi, ToplamMiktar, MalzemeBirim, BirimFiyat) VALUES (?, ?, ?, ?)";
        String updateQuery = "UPDATE Malzemeler SET ToplamMiktar = ?, BirimFiyat = ? WHERE MalzemeAdi = ?";

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            // Mevcut malzeme kontrolü
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Eğer malzeme mevcutsa, güncelle
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setFloat(1, totalQuantity); // Yeni toplam miktar
                    updateStmt.setDouble(2, unitPrice); // Yeni birim fiyat
                    updateStmt.setString(3, name);
                    updateStmt.executeUpdate();
                    System.out.println("Malzeme güncellendi: " + name);
                }
            } else {
                // Eğer malzeme mevcut değilse, yeni ekle
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, name);
                    insertStmt.setFloat(2, totalQuantity);
                    insertStmt.setString(3, unit);
                    insertStmt.setDouble(4, unitPrice);
                    insertStmt.executeUpdate();
                    System.out.println("Yeni malzeme eklendi: " + name);
                }
            }
        } catch (SQLException e) {
            System.out.println("Malzeme ekleme/güncelleme hatası...");
            e.printStackTrace();
        }
    }


    // Malzeme adını kullanarak ID'sini al
    public int getIngredientIdByName(String name) {
        String query = "SELECT MalzemeID FROM Malzemeler WHERE MalzemeAdi = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("MalzemeID");
            }
        } catch (SQLException e) {
            System.out.println("Malzeme ID alma hatası...");
            e.printStackTrace();
        }
        return -1; // Malzeme yoksa -1 döndür
    }

    // Tarif-Malzeme ilişkisinin eklenmesi
    public void addRecipeIngredient(int recipeId, int ingredientId, double ingredientAmount) {
        String query = "INSERT INTO TarifMalzeme (TarifID, MalzemeID, MalzemeMiktar) VALUES (?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, recipeId);
            stmt.setInt(2, ingredientId);
            stmt.setFloat(3, (float) ingredientAmount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Tarif malzeme ekleme hatası...");
            e.printStackTrace();
        }
    }
}
