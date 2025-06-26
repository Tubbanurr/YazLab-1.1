package org.example.k;

public class Recipe {
    private int id;
    private String name;
    private String category;
    private int preparationTime; //hazılama süresi
    private String instructions; //talimatlar
    private String imagePath; //resim yolu
    private double cost; //  maliyet
    private double matchPercentage;//eşleşme yüzdesi

    public Recipe(int id, String name, String category, int preparationTime, String instructions) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.preparationTime = preparationTime;
        this.instructions = instructions;
    }

    public Recipe(String name, String category, int preparationTime, String instructions, String imagePath, double cost) {
        this.name = name;
        this.category = category;
        this.preparationTime = preparationTime;
        this.instructions = instructions;
        this.imagePath = imagePath;
        this.cost = cost;
    }
    public Recipe(int id, String name, String category, int preparationTime, String instructions, String imagePath, double cost) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.preparationTime = preparationTime;
        this.instructions = instructions;
        this.imagePath = imagePath;
        this.cost = cost;
    }
    public Recipe(String name, String category, int preparationTime, String instructions, String imagePath, double cost, double matchPercentage) {
        this.name = name;
        this.category = category;
        this.preparationTime = preparationTime;
        this.instructions = instructions;
        this.imagePath = imagePath;
        this.cost = cost;
        this.matchPercentage = matchPercentage;
    }

    public Recipe(String name, String imagePath) {
        this.name = name;
        this.imagePath = imagePath;
    }
    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public String getInstructions() {
        return instructions;
    }
    public String getImagePath() {
        return imagePath;
    }
    public double getCost() {
        return cost;
    }
    public double getMatchPercentage(){
        return matchPercentage;
    }


    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    public void setImagePath(String imagePath){
        this.imagePath = imagePath;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }
    public void setMatchPercentage(double matchPercentage)
    {
        this.matchPercentage = matchPercentage;
    }
}
