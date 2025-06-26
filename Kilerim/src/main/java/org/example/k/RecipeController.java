package org.example.k;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.SQLException;

public class RecipeController {


    private  DatabaseManager db = new DatabaseManager();
    //KilerimApp ka = new KilerimApp();

    // Tarife göre dinamik arama ve filtreleme için tarif listesi
    public ObservableList<Recipe> recipeList = FXCollections.observableArrayList();

    @FXML
    private TextField searchField;  // Kullanıcıdan arama metnini almak için

    @FXML
    private Button searchButton;    // Arama butonu

    @FXML
    private TextField nameField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField prepTimeField;
    @FXML
    private TextArea instructionsField;
    @FXML
    private TextField costField;
    @FXML
    private ListView<String> ingredientsListView;
    @FXML
    private ListView<Recipe> recipeListView;

    @FXML
    private TextField ingredientNameField;
    @FXML
    private TextField ingredientQuantityField;
    @FXML
    private TextField ingredientUnitField;
    @FXML
    private TextField ingredientPriceField;
    @FXML
    private Button imageButton;
    @FXML
    private TextField imageField;
    @FXML
    private TextField selectedImagePathField = new TextField();


    private DatabaseManager dm;

    public void setApp(DatabaseManager dm) {
        this.dm = dm;
    }

    // Tarife göre arama ve filtreleme fonksiyonu
    @FXML
    public void initialize()
    {
        // Tarife göre dinamik arama ve filtreleme

    }

    public List<String> getCategoriesFromDatabase() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT Kategori FROM Tarifler"; // Kategorileri almak için sorgu

        try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("Kategori"));
            }
        } catch (SQLException e) {
            System.out.println("Kategori alma hatası...");
            e.printStackTrace();
        }

        return categories;
    }

    // Tarifleri veritabanından çeker
    public List<Recipe> loadRecipes() {
        List<Recipe> recipeList = new ArrayList<>(); // Tarifleri saklayacak listeyi burada oluşturuyoruz

        try {
            Connection connection = db.connect();
            Statement statement = connection.createStatement();

            // Tarifleri veritabanından çek
            String query = "SELECT * FROM Tarifler";
            ResultSet resultSet = statement.executeQuery(query);
            // Her bir tarif için veritabanındaki bilgileri al ve listeye ekle
            while (resultSet.next()) {
                int id = resultSet.getInt("TarifID");
                String name = resultSet.getString("TarifAdi");
                String category = resultSet.getString("Kategori");
                int preparationTime = resultSet.getInt("HazirlamaSuresi");
                String steps = resultSet.getString("Talimatlar");
                double cost = resultSet.getDouble("Maliyet");
                String imagePath = resultSet.getString("gorselYolu");
                // Recipe nesnesi oluştur ve listeye ekle
                Recipe recipe = new Recipe(id, name, category, preparationTime, steps, imagePath, cost);
                recipeList.add(recipe);
            }

            // Bağlantıyı kapat
            resultSet.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Tarif verisi çekme hatası...");
            e.printStackTrace();
        }

        return recipeList; // Güncellenmiş geri dönüş değeri
    }


    // Malzeme mevcut mu kontrol eder; yoksa ekler ve malzeme ID'sini döner
    public int getOrAddIngredient(String malzemeAdi, double birimFiyat, String malzemeBirim) {
        String selectSql = "SELECT MalzemeID FROM Malzemeler WHERE LOWER(MalzemeAdi) = LOWER(?)"; // Büyük/küçük harf duyarsız arama
        String insertSql = "INSERT INTO Malzemeler (MalzemeAdi, ToplamMiktar, MalzemeBirim, BirimFiyat) VALUES (?, ?, ?, ?) RETURNING MalzemeID";
        int malzemeId = -1;

        try (Connection conn = db.connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // Malzemenin veritabanında olup olmadığını kontrol et
            selectStmt.setString(1, malzemeAdi);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                // Malzeme mevcutsa malzeme ID'sini al
                malzemeId = rs.getInt("MalzemeID");
            } else {
                // Yeni malzeme ekle
                insertStmt.setString(1, malzemeAdi);
                insertStmt.setDouble(2, 0); // Toplam miktar varsayılan olarak 0
                insertStmt.setString(3, malzemeBirim);
                insertStmt.setDouble(4, birimFiyat);
                ResultSet insertRs = insertStmt.executeQuery();
                if (insertRs.next()) {
                    malzemeId = insertRs.getInt("MalzemeID");
                }
            }
        } catch (SQLException e) {
            System.out.println("Malzeme kontrol veya ekleme hatası: " + e.getMessage());
        }
        return malzemeId;
    }



    // Tarif ve Malzeme arasında eşleştirme ekleme
    public void addRecipeIngredient(int tarifid, int malzemeid, double miktar) {
        String sql = "INSERT INTO TarifMalzeme (TarifID, MalzemeID, MalzemeMiktar) VALUES (?, ?, ?)";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tarifid);
            pstmt.setInt(2, malzemeid);
            pstmt.setDouble(3, miktar);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Tarif ve malzeme eşleştirme hatası: " + e.getMessage());
        }
    }

    public boolean isRecipeExist(String recipeName, String category) {
        String query = "SELECT COUNT(*) FROM Tarifler WHERE LOWER(TarifAdi) = LOWER(?) AND LOWER(Kategori) = LOWER(?)";

        try (Connection conn = db.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, recipeName);
            stmt.setString(2, category);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return true; // Tarif mevcut
            }
        } catch (SQLException e) {
            System.out.println("Tarif kontrol hatası: " + e.getMessage());
        }
        return false; // Tarif mevcut değilse
    }

    // Yeni tarif ekler
    public int addRecipe(Recipe newRecipe) {
        String recipeQuery = "INSERT INTO Tarifler (TarifAdi, Kategori, HazirlamaSuresi, Talimatlar, Maliyet, gorselYolu ) VALUES (?, ?, ?, ?, ?, ?) RETURNING TarifID";
        int recipeId = -1;

        try (Connection conn = db.connect(); PreparedStatement recipeStmt = conn.prepareStatement(recipeQuery)) {
            // Tarif ekleme
            recipeStmt.setString(1, newRecipe.getName());
            recipeStmt.setString(2, newRecipe.getCategory());
            recipeStmt.setInt(3, newRecipe.getPreparationTime());
            recipeStmt.setString(4, newRecipe.getInstructions());
            recipeStmt.setDouble(5, newRecipe.getCost());
            recipeStmt.setString(6, newRecipe.getImagePath());

            ResultSet rs = recipeStmt.executeQuery(); // RETURNING ID için executeQuery kullanılır
            if (rs.next()) {
                recipeId = rs.getInt("TarifID"); // Dönen tarif ID'sini alın
            }

        } catch (SQLException e) {
            System.out.println("Tarif veya görsel ekleme hatası...");
            e.printStackTrace();
        }
        return recipeId;
    }



    // Tarif güncelleme işlemi
    public void updateRecipe(int id, Recipe updatedRecipe) {
        String query = "UPDATE Tarifler SET TarifID= ?, TarifAdi = ?, Kategori = ?, HazirlamaSuresi = ?, Talimatlar = ?, Maliyet = ? WHERE TarifID = ?";

        try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.setString(2, updatedRecipe.getName());
            stmt.setString(3, updatedRecipe.getCategory());
            stmt.setInt(4, updatedRecipe.getPreparationTime());
            stmt.setString(5, updatedRecipe.getInstructions());
            stmt.setDouble(6, updatedRecipe.getCost()); // Maliyet alanını ekliyoruz
            stmt.setString(7, updatedRecipe.getImagePath() );
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Tarif güncelleme hatası...");
            e.printStackTrace();
        }
    }

    // Arama sonucuna göre tarifleri filtreleyen metot
    private void filterRecipes(String query)
    {
        ObservableList<Recipe> filteredList = recipeList.stream()
                .filter(recipe -> recipe.getName().toLowerCase().contains(query))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        // Filtrelenmiş tarifleri ekrana yansıtma işlemi yapılabilir
        displayFilteredRecipes(filteredList);
    }

    // Filtrelenmiş tariflerin ekranda ListView'de gösterilmesi
    private void displayFilteredRecipes(ObservableList<Recipe> filteredList) {
        recipeListView.setItems(filteredList);  // ListView bileşeninde tarifleri güncelle
    }

    // Tarif ekleme veya güncelleme
    @FXML
    public void handleSaveRecipe() {
        String name = nameField.getText();
        String category = categoryField.getText();
        int prepTime = Integer.parseInt(prepTimeField.getText());
        String instructions = instructionsField.getText();
        String imagePath = imageField.getText(); // Seçilen resim yolu
        double cost = Double.parseDouble(costField.getText());
        // Malzemeleri topla
        List<Ingredient> ingredients = collectIngredients();

        // Tarifin var olup olmadığını kontrol et
        if (isRecipeExists(name)) {
            int recipeId = getRecipeIdByName(name); // Mevcut tarifin ID'sini al
            updateRecipe(recipeId, new Recipe(name, category, prepTime, instructions, imagePath, cost)); // Güncelle
        } else {
            addRecipe(new Recipe(name, category, prepTime, instructions, imagePath, cost)); // Yeni tarif ekle
        }

        clearFields(); // Formu temizle
    }

    // Tarifin var olup olmadığını kontrol eden metot
    private boolean isRecipeExists(String name) {
        String sql = "SELECT COUNT(*) FROM Tarifler WHERE TarifAdi = ?";
        try (Connection conn = db.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0; // Eğer sayım 0'dan büyükse tarif var demektir
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false; // Hata durumunda tarif yok sayılır
    }

    // Tarif adını kullanarak ID'sini al
    public int getRecipeIdByName(String name) {
        String sql = "SELECT TarifID FROM Tarifler WHERE TarifAdi = ?";
        try (Connection conn = db.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("TarifID") : -1; // Tarif ID'sini döndür, yoksa -1 döner
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1; // Hata durumunda -1 döner
    }



    // Malzemeleri toplar
    private List<Ingredient> collectIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();

        // ingredientsListView'daki her bir malzeme için döngü oluştur
        for (String item : ingredientsListView.getItems()) {
            // Malzeme bilgilerini parse et
            String[] parts = item.split(" - ");
            if (parts.length >= 3) {
                String name = parts[0]; // Malzeme adı
                String quantity = parts[1]; // Miktar
                String[] quantityParts = quantity.split(" ");
                float amount = Float.parseFloat(quantityParts[0]); // Miktarı float olarak al
                String unit = quantityParts[1]; // Birimi

                // Toplam miktarı varsayılan bir değerde tutabiliriz
                int totalQuantity = 0; // İhtiyacınıza göre toplam miktarı belirleyebilirsiniz

                // Yeni Ingredient nesnesi oluştur ve listeye ekle
                ingredients.add(new Ingredient(name, amount, 0.0)); // id ve unitPrice varsayılan değerlerde
            }
        }

        return ingredients; // Oluşturulan malzemeler listesini döndür
    }

    public boolean deleteRecipeById(int recipeId) {
        String deleteRecipeQuery = "DELETE FROM Tarifler WHERE TarifID = ?";
        String deleteIngredientsQuery = "DELETE FROM TarifMalzeme WHERE TarifID = ?";

        try (Connection conn = db.connect();
             PreparedStatement deleteRecipeStmt = conn.prepareStatement(deleteRecipeQuery);
             PreparedStatement deleteIngredientsStmt = conn.prepareStatement(deleteIngredientsQuery)) {

            // İlk olarak, tarifin malzemelerini ilişki tablosundan sil
            deleteIngredientsStmt.setInt(1, recipeId);
            deleteIngredientsStmt.executeUpdate();

            // Ardından, tarifi ana tarif tablosundan sil
            deleteRecipeStmt.setInt(1, recipeId);
            int rowsAffected = deleteRecipeStmt.executeUpdate();

            return rowsAffected > 0; // Silme başarılıysa true döndür

        } catch (SQLException e) {
            System.out.println("Tarif silme hatası: " + e.getMessage());
            return false;
        }
    }

    // Alanları temizle
    private void clearFields() {
        nameField.clear();
        categoryField.clear();
        prepTimeField.clear();
        instructionsField.clear();
        ingredientNameField.clear();
        ingredientQuantityField.clear();
        ingredientUnitField.clear();
        ingredientPriceField.clear();
    }

    // Malzeme ekleme
    @FXML
    public void handleAddIngredient() {
        String name = ingredientNameField.getText();
        String quantity = ingredientQuantityField.getText();
        String unit = ingredientUnitField.getText();
        String price = ingredientPriceField.getText();

        // Malzemeyi listeye ekleyin
        ingredientsListView.getItems().add(name + " - " + quantity + " " + unit + " - " + price);
        clearIngredientFields();
    }

    // Malzeme alanlarını temizle
    private void clearIngredientFields() {
        ingredientNameField.clear();
        ingredientQuantityField.clear();
        ingredientUnitField.clear();
        ingredientPriceField.clear();
    }
}
