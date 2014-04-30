package classes;

import java.io.Serializable;
import java.nio.charset.CharacterCodingException;
import java.util.Collections;
import java.util.Comparator;
import org.apache.hadoop.io.Text;

/**
 * Comparator for SEM IDs.
 * @author Alessandro Menti
 */
public class SEMIDComparator implements Comparator<Text>, Serializable {
    /**
     * UID used for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Compares <tt>o1</tt> with <tt>o2</tt>. Note: this comparator imposes
     * orderings that are inconsistent with <tt>equals</tt>.
     * @param o1 The first <tt>Text</tt> object
     * @param o2 The second <tt>Text</tt> object
     * @return If both objects have got a numeric ID, the comparator returns
     * <tt>-1</tt> if and only if the numeric ID of <tt>o1</tt> is less than
     * the one of <tt>o2</tt>, <tt>0</tt> if they are equal, and <tt>1</tt>
     * otherwise. If at least an object has no numeric ID, the comparison is
     * performed on the entire Text objects.
     */
    @Override
    public int compare(Text o1, Text o2) {
        /* Note to implementers: see the Javadoc for Comparator to learn
         * about the contract requirements. */
/*
In the foregoing description, the notation sgn(expression)
designates the mathematical signum function, which is defined to return one of -1, 0,
or 1 according to whether the value of expression is negative, zero or positive.

The implementor must ensure that sgn(compare(x, y)) == -sgn(compare(y, x)) for all x
and y. (This implies that compare(x, y) must throw an exception if and only if
compare(y, x) throws an exception.)

The implementor must also ensure that the relation is transitive: ((compare(x, y)>0)
&& (compare(y, z)>0)) implies compare(x, z)>0.

Finally, the implementor must ensure that compare(x, y)==0 implies that
sgn(compare(x, z))==sgn(compare(y, z)) for all z.

It is generally the case, but not strictly required that (compare(x, y)==0) ==
(x.equals(y)). Generally speaking, any comparator that violates this condition should
clearly indicate this fact. The recommended language is "Note: this comparator imposes
orderings that are inconsistent with equals."
*/
        // Check if both members start with a numeric ID. If not, make
        // the comparison on the entire text object.
        int firstIDPos = o1.find("-");
        int secondIDPos = o2.find("-");
        if ((firstIDPos == -1) || (secondIDPos == -1)) {
            return o1.compareTo(o2);
        }
        // Compare the objects using the numeric IDs.
        Long firstID;
        try {
            firstID = Long.parseLong(Text.decode(o1.copyBytes(), 0, firstIDPos - 1));
        } catch (NumberFormatException e) {
            firstID = (long) 0;
        } catch (CharacterCodingException e) {
            firstID = (long) 0;
        }
        Long secondID;
        try {
            secondID = Long.parseLong(Text.decode(o2.copyBytes(), 0, secondIDPos - 1));
        } catch (NumberFormatException e) {
            secondID = (long) 0;
        } catch (CharacterCodingException e) {
            secondID = (long) 0;
        }
        return firstID.compareTo(secondID);
    }
    
    public static void main(String args[]) {
    	java.util.List<Text> t = new java.util.LinkedList<Text>();
    	t.add(new Text("0-1425"));
    	t.add(new Text("128-FINAL"));
    	t.add(new Text("61-11025"));
    	
    	System.out.println(t.toString());
    	
    	Collections.sort(t, new SEMIDComparator());

    	System.out.println(t.toString());
    	
    }

}
