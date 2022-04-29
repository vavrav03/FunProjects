package gui;

/**
 * Třída sloužící jako poskytovatel informace o tom, kolikrát je panel se hrou zvětšen oproti normálnímu stavu
 */
public class RatioHolder {
   
   private double ratio;

   public RatioHolder(){
      this.ratio = 1;
   }

   public double getRatio() {
      return ratio;
   }

   public void setRatio(double ratio) {
      this.ratio = ratio;
   }

   public int sizeInRatio(int size){
      return (int) (ratio * size);
   }

   public int sizeInRatio(double size){
      return (int) (ratio * size);
   }

   /**
    * 
    * @param size size ve zvětšeném poměru
    * @return hodnota sizei v základní sizei
    */
   public int sizeInBaseRatio(int size){
      return (int) (size / ratio);
   }
}
