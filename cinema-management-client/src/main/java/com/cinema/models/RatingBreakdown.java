package com.cinema.models;

public class RatingBreakdown {
    private int fiveStar;
    private int fourStar;
    private int threeStar;
    private int twoStar;
    private int oneStar;

    public RatingBreakdown() {}

    public RatingBreakdown(int fiveStar, int fourStar, int threeStar, int twoStar, int oneStar) {
        this.fiveStar = fiveStar;
        this.fourStar = fourStar;
        this.threeStar = threeStar;
        this.twoStar = twoStar;
        this.oneStar = oneStar;
    }

    public int getFiveStar() { return fiveStar; }
    public void setFiveStar(int fiveStar) { this.fiveStar = fiveStar; }

    public int getFourStar() { return fourStar; }
    public void setFourStar(int fourStar) { this.fourStar = fourStar; }

    public int getThreeStar() { return threeStar; }
    public void setThreeStar(int threeStar) { this.threeStar = threeStar; }

    public int getTwoStar() { return twoStar; }
    public void setTwoStar(int twoStar) { this.twoStar = twoStar; }

    public int getOneStar() { return oneStar; }
    public void setOneStar(int oneStar) { this.oneStar = oneStar; }

    public int getTotal() {
        return fiveStar + fourStar + threeStar + twoStar + oneStar;
    }

    public double getPercentage(int stars) {
        int total = getTotal();
        if (total == 0) return 0;
        
        int count = switch (stars) {
            case 5 -> fiveStar;
            case 4 -> fourStar;
            case 3 -> threeStar;
            case 2 -> twoStar;
            case 1 -> oneStar;
            default -> 0;
        };
        
        return (double) count / total * 100;
    }
}