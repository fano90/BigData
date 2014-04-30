
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author Mattia Zago <info@zagomattia.it>
 */
public class ImageExtractor {
    
    public static int rgb_to_int_max = 16777216;
    
    public static void main(String[] args) throws Exception{
        long time_all_start = System.currentTimeMillis();        
        char hex = '1';
        
        
        //do {
            
        String base_name_file = "C:\\Users\\Mattia\\Desktop\\ChrY\\1\\"+hex+".MEM.";
        MEM(base_name_file, hex, 10, 0, time_all_start, 10);
        MEM(base_name_file, hex, 9, 0, time_all_start, 9);
        MEM(base_name_file, hex, 8, 0, time_all_start, 8);
        MEM(base_name_file, hex, 7, 0, time_all_start, 7);
        MEM(base_name_file, hex, 6, 0, time_all_start, 6);
        MEM(base_name_file, hex, 5, 0, time_all_start, 5);
        MEM(base_name_file, hex, 4, 0, time_all_start, 4);
        MEM(base_name_file, hex, 3, 0, time_all_start, 3);
        
            
        //} while((count_changed*100/(double)count) < 1);
    }
    
    public static int getIntRGB(int soglia_max, int current_value) {
        return (rgb_to_int_max*current_value / soglia_max);
    }
    public static int[] getRGBComponents(int RGBValue) {
        int[] result = new int[3];
        
            result[0] = RGBValue % 65536; //r
            RGBValue -= 65536 * result[0];
            result[1] = RGBValue % 256; //g
            RGBValue -= 256 * result[1];
            result[2] = RGBValue; //b
        
        return result;
    }
    
    public static void MEM(String base_name_file, char hex, int soglia_max, int soglia_min, long time_all_start, int prog) throws Exception{
            int count_changed = 0;
            int sum = 0;
            int count = 0;
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            
            // red, green, blue in [0-255]. Need 3 cells for every pixel in img
            int w = 4096, h = 4096;
            int[] pixels = new int[w*h*3];
            for(int i=0; i<pixels.length; i+=3) {pixels[i]=255;pixels[i+1]=0;pixels[i+2]=0;}
            //save(pixels, h, w, base_name_file+"clean.png");

            PrintWriter writer = new PrintWriter(base_name_file+"clean", "UTF-8");

            BufferedReader br = new BufferedReader(new FileReader(new File(base_name_file+"matrix"))); 
            
            System.out.println("\n#====================================================");
            System.out.println("| Scansione della matrice "+hex+"-MEM in corso");
            int line_count = 0;
            int pixel_count = -1;
            for(String line; (line = br.readLine()) != null; line_count++) {
                if(line.length()==0) continue;
                String[] values = line.split(";");
                for(int i=0; i<values.length; i++) {
                    int val = Integer.valueOf(values[i]);
                    sum+=val;
                    count++;
                    if(!take(val, soglia_max, soglia_min)) {
                        count_changed++;
                        val=-1;
                    } else {
                        if(val>=max) max = val;
                        if(val<=min) min = val;
                        int[] tmp_pixel = getRGBComponents(getIntRGB(soglia_max, val));
                        pixel_count++;
                        pixels[pixel_count] = tmp_pixel[0];
                        pixel_count++;
                        pixels[pixel_count] = tmp_pixel[1];
                        pixel_count++;
                        pixels[pixel_count] = tmp_pixel[2];
                    }
                    writer.write(val+";");
                }
                writer.write("\n");
                //System.out.println("| Line "+line_count+": "+Tools.time(time_all_start));
            }

            System.out.println("| Processo completato in: "+Tools.time(time_all_start));
            System.out.println("#====================================================");
            
            save(pixels, h, w, base_name_file+prog+".png");
            
            System.out.println("| Processo completato in: "+Tools.time(time_all_start));
            System.out.println("#====================================================");
            System.out.println("| Changed: "+(count_changed)+"/"+count+" - "+(count_changed*100/(double)count));
            System.out.println("| Max: "+max+" - Min: "+min+" - Media: "+(sum/count));
            System.out.println("| Soglia Max: "+soglia_max);
            System.out.println("| Processo completato in: "+Tools.time(time_all_start));
            System.out.println("#====================================================");
            
            soglia_max = max - 1;
    }
        
    public static void save(int[] pixels, int h, int w, String path) throws Exception {
        long time_img_start = System.currentTimeMillis();
            System.out.print("| Generazione ImageIcon");
            BufferedImage image = new BufferedImage(4096, 4096, BufferedImage.TYPE_INT_RGB);
            WritableRaster raster = image.getRaster();
            raster.setPixels(0, 0, w, h, pixels);
            ImageIcon icon = new ImageIcon(image);
            System.out.print(" - DONE: "+Tools.time(time_img_start)+"\n");
            
            System.out.print("| Conversione Image");
            Image img = icon.getImage();
            System.out.print(" - DONE: "+Tools.time(time_img_start)+"\n");
            
            System.out.print("| Generazione BufferedImage");
            BufferedImage bi = new BufferedImage(img.getWidth(null),img.getHeight(null),BufferedImage.TYPE_INT_RGB);
            System.out.print(" - DONE: "+Tools.time(time_img_start)+"\n");
            
            System.out.print("| Drawing");
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
            System.out.print(" - DONE: "+Tools.time(time_img_start)+"\n");
            
            System.out.print("| Salva PNG ("+path+") ");
            ImageIO.write(bi, "png", new File(path));
            System.out.print(" - DONE: "+Tools.time(time_img_start)+"\n");
    }
    
    public static boolean take(int num, int max, int min) {
        return (num<max && num>=min);
    }
    
    public static Image getImageFromArray(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = (WritableRaster) image.getData();
        raster.setPixels(0,0,width,height,pixels);
        return image;
    }
}
