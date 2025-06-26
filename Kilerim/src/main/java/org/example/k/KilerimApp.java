package org.example.k;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.*;

public class KilerimApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    RecipeController rc;
    DatabaseManager db;
    private Stage addRecipeStage = null; //showaddrecipe için
    private Stage detailStage = null; // Detay ekranı için sınıf değişkeni
    TilePane recipeImagePane = new TilePane(30, 30); // Resimler için TilePane
    ListView<String> recipeListView = new ListView<>();
    TextField ingredientSearchField = new TextField(); // Malzeme arama alanı
    VBox filterLayout = createFilterLayout(); // Filtreleme alanını burada oluşturuyoruz
    TextField searchField = new TextField();
    // Mevcut Malzemeler Listesi
    private List<Ingredient> userIngredients = new ArrayList<>();


    @Override
    public void start(Stage primaryStage) {
        rc = new RecipeController();
        db = new DatabaseManager();

        HBox searchAndFilterLayout = new HBox(10);
        searchAndFilterLayout.setPadding(new Insets(10));
        searchAndFilterLayout.setAlignment(Pos.CENTER);

        // Arama alanı

        searchField.setPromptText("Tarif Ara");

        // Malzeme arama alanı
        ingredientSearchField.setPromptText("Malzeme Ara");

        // Filtreleme butonu
        Button filterButton = new Button("Filtrele");
        filterLayout.setVisible(false); // Başlangıçta filtreleme alanını gizle
        filterButton.setOnAction(e -> toggleFilterLayout());

        // Arama butonu
        Button searchButton = new Button("Ara");
        searchButton.setOnAction(e -> {
            String query = searchField.getText();
            searchRecipes(query); // Her harf girişi ile aramayı güncelle
        });


        // Malzeme arama butonu
        Button ingredientSearchButton = new Button("Malzemeye Göre Ara");
        ingredientSearchButton.setOnAction(e -> searchByIngredient());


        // Tarif ekleme butonu
        Button addRecipeButton = new Button("Yeni Tarif Ekle");
        addRecipeButton.setOnAction(e -> showAddRecipeForm(primaryStage));

        // Resimler için ScrollPane ayarlamaları
        recipeImagePane.setPadding(new Insets(5));
        recipeImagePane.setPrefColumns(3); // Daha az sütun ile daha büyük alan
        recipeImagePane.setPrefHeight(500); // Yüksekliği
        recipeImagePane.setPrefWidth(800); // Genişliği artırabilirsiniz

        ScrollPane scrollPane = new ScrollPane(recipeImagePane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true); // Yüksekliği otomatik ayarla
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Layout’a bileşenleri ekliyoruz
        VBox layout = new VBox(20, searchAndFilterLayout, filterLayout, scrollPane);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        // Tarif ekleme ve diğer arama bileşenleri aynı satıra eklendi
        searchAndFilterLayout.getChildren().addAll(searchField, searchButton, ingredientSearchField, ingredientSearchButton, filterButton, addRecipeButton);

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        try {
            List<Recipe> recipes = rc.loadRecipes(); // Veritabanından tarifleri yükleyin
            if (recipes.isEmpty()) {
                System.out.println("Veritabanında tarif bulunamadı.");
            } else {
                showRecipesWithImages(recipes); // Tarifleri buton şeklinde ana ekranda gösterin
            }
        } catch (Exception e) {
            System.out.println("Tarifleri yüklerken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showRecipesWithImages(List<Recipe> recipes) {
        recipeImagePane.getChildren().clear(); // Önce mevcut tarifleri temizleyin

        for (Recipe recipe : recipes) {
            // Tarifi temsil eden bir görsel oluştur
            ImageView imageView = new ImageView(new Image(recipe.getImagePath()));
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);

            // Tarife tıklandığında detayları göster
            imageView.setOnMouseClicked(e -> showRecipeDetails(recipe));

            // Tarif adını ve eşleşme yüzdesini gösteren bir etiket ekleyin
            Label recipeLabel = new Label(recipe.getName() + " (" + recipe.getMatchPercentage() + "%)");

            // Tarifi gösteren bir VBox oluşturun
            VBox recipeBox = new VBox(imageView, recipeLabel);
            recipeBox.setAlignment(Pos.CENTER);
            recipeImagePane.getChildren().add(recipeBox);
        }
    }


    // Yeni tarif ekleme formu
    public void showAddRecipeForm(Stage primaryStage) {
        // Ekran zaten açıksa yenisini açma
        if (addRecipeStage != null && addRecipeStage.isShowing()) {
            addRecipeStage.toFront(); // Zaten açıksa ön plana getir
            return;
        }

        // Yeni ekran oluştur ve ayarları yap
        addRecipeStage = new Stage();
        addRecipeStage.setTitle("Yeni Tarif Ekle");

        TextField recipeNameField = new TextField();
        recipeNameField.setPromptText("Tarif Adı");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Kategori");

        TextField preparationTimeField = new TextField();
        preparationTimeField.setPromptText("Hazırlama Süresi (dk)");

        TextField costField = new TextField();
        costField.setPromptText("Maliyet");

        TextArea ingredientsField = new TextArea();
        ingredientsField.setPromptText("Malzemeler (her bir malzemeyi yeni bir satıra yazın)");

        TextArea ingredientAmountsField = new TextArea();
        ingredientAmountsField.setPromptText("Malzemelerin Miktarları (her satıra karşılık gelecek şekilde)");

        TextArea ingredientUnitsField = new TextArea();
        ingredientUnitsField.setPromptText("Malzeme Birimleri (örneğin: 'gram', 'adet')");

        TextArea ingredientPricesField = new TextArea();
        ingredientPricesField.setPromptText("Malzeme Birim Fiyatları (her satıra karşılık gelen fiyat)");

        TextField instructionsField = new TextField();
        instructionsField.setPromptText("Talimatlar");

        Button addImageButton = new Button("Resim Ekle");
        FileChooser fileChooser = new FileChooser();
        final File[] selectedImageFile = new File[1];

        // Resim seçme işlemi
        addImageButton.setOnAction(e -> {
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File chosenFile = fileChooser.showOpenDialog(addRecipeStage);
            if (chosenFile != null) {
                selectedImageFile[0] = chosenFile;
                System.out.println("Seçilen resim: " + chosenFile.getAbsolutePath());
            }
        });

        Button addRecipeButton = new Button("Tarifi Ekle");
        addRecipeButton.setOnAction(e -> {
            // Formdan gelen veriler
            String recipeName = recipeNameField.getText();
            String category = categoryField.getText();
            int preparationTime;
            try {
                preparationTime = Integer.parseInt(preparationTimeField.getText());
            } catch (NumberFormatException ex) {
                System.out.println("Hazırlama süresi geçerli bir sayı olmalıdır.");
                return;
            }
            String instructions = instructionsField.getText();
            String ingredientsText = ingredientsField.getText();
            String amountsText = ingredientAmountsField.getText();
            String unitsText = ingredientUnitsField.getText();
            String pricesText = ingredientPricesField.getText();

            double costText;
            try {
                costText = Double.parseDouble(costField.getText());
            } catch (NumberFormatException ex) {
                System.out.println("Maliyet geçerli bir sayı olmalıdır.");
                return;
            }


            String[] ingredientsArray = ingredientsText.split("\n");
            String[] amountsArray = amountsText.split("\n");
            String[] unitsArray = unitsText.split("\n");
            String[] pricesArray = pricesText.split("\n");

            // Tarifin zaten olup olmadığını kontrol et
            if (rc.isRecipeExist(recipeName, category)) { // Tarif kontrol fonksiyonu çağrılıyor
                System.out.println("Bu tarif zaten mevcut.");
                return; // Mevcut tarif varsa işlem sonlandırılıyor
            }

            // Resim seçildi mi kontrolü
            if (selectedImageFile[0] != null) {
                Recipe newRecipe = new Recipe(recipeName, category, preparationTime, instructions, selectedImageFile[0].getAbsolutePath(), costText);

                // Tarif ekleme ve dönen ID'yi alma
                int recipeId = rc.addRecipe(newRecipe);

                // Malzemeleri ekleme
                for (int i = 0; i < ingredientsArray.length; i++) {
                    if (!ingredientsArray[i].trim().isEmpty()) {
                        String ingredient = ingredientsArray[i];
                        String amountStr = amountsArray[i].trim();
                        double amount = 0;

                        // Miktar geçerliliğini kontrol et
                        if (!amountStr.isEmpty()) {
                            try {
                                amount = Double.parseDouble(amountStr);
                            } catch (NumberFormatException ex) {
                                System.out.println("Geçersiz miktar: " + amountStr);
                                continue; // Geçersiz miktar durumunda döngünün devamı
                            }
                        }

                        // Unit ve price için varsayılan değerler
                        String unit = (i < unitsArray.length) ? unitsArray[i].trim() : "adet"; // Varsayılan birim "adet"
                        double price = (i < pricesArray.length) ? Double.parseDouble(pricesArray[i].trim()) : 0; // Varsayılan fiyat 0

                        // Malzeme ekleme ve tarifle eşleştirme
                        int ingredientId = rc.getOrAddIngredient(ingredient, price, unit);
                        rc.addRecipeIngredient(recipeId, ingredientId, amount);
                    }
                }

                // Formu kapatma
                addRecipeStage.setOnCloseRequest(event -> addRecipeStage = null); // Kapanınca değişkeni sıfırla
            } else {
                System.out.println("Lütfen bir resim seçin.");
            }
        });

        VBox addRecipeLayout = new VBox(10, recipeNameField, categoryField, preparationTimeField, costField,
                ingredientsField, ingredientAmountsField, ingredientUnitsField, ingredientPricesField, instructionsField,
                addImageButton, addRecipeButton);
        addRecipeLayout.setPadding(new Insets(20));
        Scene addRecipeScene = new Scene(addRecipeLayout, 400, 600);
        addRecipeStage.setScene(addRecipeScene);
        addRecipeStage.show();
    }

    private VBox createFilterLayout() {
        VBox filterLayout = new VBox(10);
        filterLayout.setPadding(new Insets(10));

        ComboBox<String> filterOptions = new ComboBox<>();
        filterOptions.getItems().addAll(
                "Maliyet (Artan)",
                "Maliyet (Azalan)",
                "Hazırlama Süresi (Artan)",
                "Hazırlama Süresi (Azalan)"
        );

        TextField minCostField = new TextField();
        minCostField.setPromptText("Minimum Maliyet");
        TextField maxCostField = new TextField();
        maxCostField.setPromptText("Maksimum Maliyet");
        TextField ingredientCountField = new TextField();
        ingredientCountField.setPromptText("Malzeme Sayısı");

        Button applyFiltersButton = new Button("Uygula");
        Button clearFiltersButton = new Button("Temizle");

        filterLayout.getChildren().addAll(
                filterOptions,
                new Label("Maliyet Aralığı:"),
                minCostField,
                maxCostField,
                new Label("Malzeme Sayısı:"),
                ingredientCountField,
                applyFiltersButton,
                clearFiltersButton
        );

        // Filtre Uygula Butonuna Tıklama Olayı
        applyFiltersButton.setOnAction(event -> {
            String selectedFilter = filterOptions.getValue();
            String minCost = minCostField.getText();
            String maxCost = maxCostField.getText();
            String ingredientCount = ingredientCountField.getText();
            String searchTerm = searchField.getText();
            // Filtreleme işlemi
            filterRecipes(selectedFilter, minCost, maxCost, ingredientCount, searchTerm);
        });

        // Filtre Temizleme Butonuna Tıklama Olayı
        clearFiltersButton.setOnAction(event -> {
            filterOptions.setValue(null);
            minCostField.clear();
            maxCostField.clear();
            ingredientCountField.clear();
            showAllRecipes();
        });

        return filterLayout;
    }



    public void showAllRecipes() {
        List<Recipe> allRecipes = new ArrayList<>();

        String sql = "SELECT * FROM Tarifler"; // Tüm tarifleri seçin

        try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String recipeName = rs.getString("TarifAdi");
                String category = rs.getString("Kategori");
                int prepTimeValue = rs.getInt("HazirlamaSuresi");
                double costValue = rs.getDouble("Maliyet");
                String instructions = rs.getString("Talimatlar");
                String imagePath = rs.getString("gorselYolu");

                // Veritabanından çekilen bilgilerle Recipe nesnesi oluştur
                Recipe recipe = new Recipe(recipeName, category, prepTimeValue, instructions, imagePath, costValue);
                allRecipes.add(recipe);
            }
        } catch (SQLException e) {
            System.out.println("Tüm tarifleri alma hatası...");
            e.printStackTrace();
        }

        showRecipesWithImages(allRecipes); // Tüm tarifleri göster
    }


    public void toggleFilterLayout() {
        filterLayout.setVisible(!filterLayout.isVisible()); // Filtreleme alanını aç/kapa
    }

    @FXML
    public void searchRecipes(String query) {
        List<Recipe> results = new ArrayList<>();
        String sql = "SELECT * FROM Tarifler WHERE TarifAdi ILIKE ?";

        try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String recipeName = rs.getString("TarifAdi");
                String category = rs.getString("Kategori");
                int prepTimeValue = rs.getInt("HazirlamaSuresi");
                double costValue = rs.getDouble("Maliyet");
                String instructions = rs.getString("Talimatlar");
                String imagePath = rs.getString("gorselYolu");

                // Eşleşme yüzdesini hesapla
                int matchPercentage = calculateMatchPercentage(recipeName, query);

                // Veritabanından çekilen bilgilerle Recipe nesnesi oluştur
                Recipe recipe = new Recipe(recipeName, category, prepTimeValue, instructions, imagePath, costValue);
                recipe.setMatchPercentage(matchPercentage); // Eşleşme yüzdesini Recipe nesnesine ekle
                results.add(recipe);
            }
        } catch (SQLException e) {
            System.out.println("Arama hatası...");
            e.printStackTrace();
        }

        showRecipesWithImages(results);
    }

    public void searchByIngredient() {
        String ingredient = ingredientSearchField.getText();
        List<Recipe> results = new ArrayList<>();
        String sql = "SELECT t.*, COUNT(*) AS match_count, " +
                "(SELECT COUNT(*) FROM TarifMalzeme WHERE TarifID = t.TarifID) AS total_ingredients " +
                "FROM Tarifler t " +
                "JOIN Tarifmalzeme tm ON t.TarifID = tm.TarifID " +
                "JOIN Malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                "WHERE m.MalzemeAdi ILIKE ? " +
                "GROUP BY t.TarifAdi, t.TarifID";

        try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + ingredient + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String recipeName = rs.getString("TarifAdi");
                String category = rs.getString("Kategori");
                int prepTimeValue = rs.getInt("HazirlamaSuresi");
                double costValue = rs.getDouble("Maliyet");
                String instructions = rs.getString("Talimatlar");
                String imagePath = rs.getString("gorselYolu");

                // Eşleşme yüzdesini hesapla
                int matchCount = rs.getInt("match_count");
                int totalIngredients = rs.getInt("total_ingredients");
                double matchPercentage = (double) matchCount / totalIngredients * 100;

                // Recipe nesnesini oluştur ve ekle
                Recipe recipe = new Recipe(recipeName, category, prepTimeValue, instructions, imagePath, costValue);
                recipe.setMatchPercentage((int) matchPercentage); // Eşleşme yüzdesini Recipe nesnesine ekle
                results.add(recipe);
            }
        } catch (SQLException e) {
            System.out.println("Malzeme arama hatası...");
            e.printStackTrace();
        }

        showRecipesWithImages(results);
    }

    // Eşleşme yüzdesini hesaplayan metod
    private int calculateMatchPercentage(String recipeName, String searchTerm) {
        if (recipeName.toLowerCase().contains(searchTerm.toLowerCase())) {
            int matchLength = searchTerm.length();
            int totalLength = recipeName.length();
            return (int) ((double) matchLength / totalLength * 100); // Yüzde hesaplama
        }
        return 0; // Eşleşme yoksa yüzde 0
    }

    private void filterRecipes(String selectedFilter, String minCost, String maxCost, String ingredientCount, String searchTerm) {
        List<Recipe> filteredRecipes = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Tarifler WHERE 1=1");

        // Arama terimi filtresi
        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql.append(" AND TarifAdi ILIKE ?"); // ILIKE, büyük/küçük harfe duyarsız arama yapar.
        }

        // Malzeme sayısı filtresi
        if (ingredientCount != null && !ingredientCount.isEmpty()) {
            sql.append(" AND (SELECT COUNT(*) FROM TarifMalzeme WHERE TarifID = Tarifler.TarifID) = ?");
        }

        // Maliyet filtreleri
        if (minCost != null && !minCost.isEmpty()) {
            sql.append(" AND Maliyet >= ?");
        }

        if (maxCost != null && !maxCost.isEmpty()) {
            sql.append(" AND Maliyet <= ?");
        }

        // Sıralama
        if (selectedFilter != null) {
            if (selectedFilter.equals("Maliyet (Artan)")) {
                sql.append(" ORDER BY Maliyet ASC");
            } else if (selectedFilter.equals("Maliyet (Azalan)")) {
                sql.append(" ORDER BY Maliyet DESC");
            } else if (selectedFilter.equals("Hazırlama Süresi (Artan)")) {
                sql.append(" ORDER BY HazirlamaSuresi ASC");
            } else if (selectedFilter.equals("Hazırlama Süresi (Azalan)")) {
                sql.append(" ORDER BY HazirlamaSuresi DESC");
            }
        }

        try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int index = 1;

            // Arama terimi için değer atama
            if (searchTerm != null && !searchTerm.isEmpty()) {
                stmt.setString(index++, "%" + searchTerm + "%"); // Arama teriminde wildcard kullanıyoruz.
            }

            // Malzeme sayısı filtresi için değer atama
            if (ingredientCount != null && !ingredientCount.isEmpty()) {
                stmt.setInt(index++, Integer.parseInt(ingredientCount));
            }

            // Maliyet filtreleri için değer atama
            if (minCost != null && !minCost.isEmpty()) {
                stmt.setDouble(index++, Double.parseDouble(minCost));
            }

            if (maxCost != null && !maxCost.isEmpty()) {
                stmt.setDouble(index++, Double.parseDouble(maxCost));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String recipeName = rs.getString("TarifAdi");
                String category = rs.getString("Kategori");
                int prepTimeValue = rs.getInt("HazirlamaSuresi");
                double costValue = rs.getDouble("Maliyet");
                String instructions = rs.getString("Talimatlar");
                String imagePath = rs.getString("gorselYolu");

                // Veritabanından çekilen bilgilerle Recipe nesnesi oluştur
                Recipe recipe = new Recipe(recipeName, category, prepTimeValue, instructions, imagePath, costValue);
                filteredRecipes.add(recipe);
            }
        } catch (SQLException e) {
            System.out.println("Filtreleme hatası...");
            e.printStackTrace();
        }

        // Filtrelenen tarifleri ana ekranda güncelleyin
        showRecipesWithImages(filteredRecipes);
    }



    // Ingredient nesnesini isme göre bulacak bir metot. Burada veritabanı sorgusu yapabilirsiniz.
    public Ingredient findIngredientByName(String name) {
        Ingredient ingredient = null;
        String sql = "SELECT * FROM Malzemeler WHERE MalzemeAdi = ?";

        try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int malzemeid = rs.getInt("MalzemeID");
                String ingredientName = rs.getString("MalzemeAdi");
                float totalQuantity = rs.getFloat("ToplamMiktar"); // Malzeme miktarı
                String unit = rs.getString("MalzemeBirim");
                double unitPrice = rs.getDouble("BirimFiyat"); // Birim fiyat

                ingredient = new Ingredient(malzemeid, ingredientName, totalQuantity, unit, unitPrice); // Ingredient nesnesi oluştur
            }
        } catch (SQLException e) {
            System.out.println("Malzeme bulunamadı...");
            e.printStackTrace();
        }
        return ingredient;
    }

    // Malzeme isimlerinden Ingredient nesneleri oluşturacak metot
    public List<Ingredient> getIngredientObjects(List<String> ingredientNames) {
        List<Ingredient> ingredients = new ArrayList<>();

        for (String name : ingredientNames) {
            Ingredient ingredient = findIngredientByName(name); // isme göre malzemeyi bul
            if (ingredient != null) {
                ingredients.add(ingredient); // Ingredient listesine ekle
            }
        }
        return ingredients;
    }


    // Tarifin maliyetini hesaplayan metod
    public double calculateRecipeCost(List<Ingredient> ingredients) {
        double totalCost = 0;
        for (Ingredient ingredient : ingredients) {
            totalCost += ingredient.getAmount() * ingredient.getUnitPrice(); // Ingredient'ın fiyat ve miktarına erişiyoruz
        }
        return totalCost;
    }


    public void showRecipeDetails(Recipe recipe) {
        // Eğer ekran zaten açıksa, yenisini açma
        if (detailStage != null && detailStage.isShowing()) {
            detailStage.toFront();
            return;
        }

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        if (recipe.getId() == 0) {
            String idQuery = "SELECT TarifID FROM Tarifler WHERE TarifAdi = ?";
            try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(idQuery)) {
                stmt.setString(1, recipe.getName());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int recipeId = rs.getInt("TarifID");
                    recipe.setId(recipeId);
                }
            } catch (SQLException e) {
                System.out.println("Tarif ID'si çekilirken hata oluştu.");
                e.printStackTrace();
                return;
            }
        }

        detailStage = new Stage();
        Label nameLabel = new Label("Tarif: " + recipe.getName());
        layout.getChildren().add(nameLabel);

        // Tarifin kategori, hazırlama süresi ve talimatlarını veritabanından çek
        String sql = "SELECT Kategori, HazirlamaSuresi, Talimatlar FROM Tarifler WHERE TarifAdi = ?";
        try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipe.getName());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String category = rs.getString("Kategori");
                int prepTime = rs.getInt("HazirlamaSuresi");

                Label categoryLabel = new Label("Kategori: " + category);
                Label prepTimeLabel = new Label("Hazırlama Süresi: " + prepTime + " dakika");

                layout.getChildren().addAll(categoryLabel, prepTimeLabel);
            }
        } catch (SQLException e) {
            System.out.println("Tarif bilgileri çekilirken hata oluştu.");
            e.printStackTrace();
        }

        // Malzemeleri ve maliyeti veritabanından çek
        String ingredientSql = "SELECT m.MalzemeAdi, tm.MalzemeMiktar, m.BirimFiyat FROM TarifMalzeme tm " +
                "JOIN Malzemeler m ON tm.MalzemeID = m.MalzemeID WHERE tm.TarifID = ?";
        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(ingredientSql)) {
            stmt.setInt(1, recipe.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String ingredientName = rs.getString("MalzemeAdi");
                double amount = rs.getDouble("MalzemeMiktar");
                double unitPrice = rs.getDouble("BirimFiyat");

                ingredients.add(new Ingredient(ingredientName, amount, unitPrice));
            }
        } catch (SQLException e) {
            System.out.println("Malzeme bilgileri çekilirken hata oluştu.");
            e.printStackTrace();
        }

        double totalCost = calculateRecipeCost(ingredients);
        Label costLabel = new Label("Toplam Maliyet: " + String.format("%.2f", totalCost) + " TL");

        ListView<String> ingredientListView = new ListView<>();
        for (Ingredient ingredient : ingredients) {
            String ingredientText = ingredient.getName() + " - " + ingredient.getAmount() + " (" + ingredient.getUnitPrice() + " TL)";
            ingredientListView.getItems().add(ingredientText);
        }

        layout.getChildren().addAll(new Label("Malzemeler:"), ingredientListView, costLabel);

        String instructions = null;
        try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipe.getName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                instructions = rs.getString("Talimatlar");
            }
        } catch (SQLException e) {
            System.out.println("Talimatlar çekilirken hata oluştu.");
            e.printStackTrace();
        }

        TextArea instructionsArea = new TextArea(instructions);
        instructionsArea.setWrapText(true);
        instructionsArea.setEditable(false);
        layout.getChildren().addAll(new Label("Talimatlar:"), instructionsArea);

        // Tarif silme butonu
        Button deleteRecipeButton = new Button("Tarif Sil");
        deleteRecipeButton.setOnAction(ex -> {
            int selectedRecipeId = recipe.getId();
            if (selectedRecipeId > 0) {
                boolean isDeleted = rc.deleteRecipeById(selectedRecipeId);
                if (isDeleted) {
                    System.out.println("Tarif başarıyla silindi.");
                    detailStage.close(); // Tarifi silince ekranı kapat
                } else {
                    System.out.println("Tarif silinemedi.");
                }
            } else {
                System.out.println("Silinecek bir tarif seçin.");
            }
        });

        layout.getChildren().add(deleteRecipeButton);

        Scene scene = new Scene(layout, 400, 600);
        detailStage.setScene(scene);
        detailStage.show();
        detailStage.setOnCloseRequest(event -> detailStage = null);
    }

}