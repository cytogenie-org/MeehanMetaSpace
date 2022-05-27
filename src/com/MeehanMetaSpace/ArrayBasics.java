package com.MeehanMetaSpace;
import java.util.Arrays;
public class ArrayBasics {
	   public enum Direction {TOP, UP,DOWN, BOTTOM};
	   private static boolean contains(final int []a, final int arg) {
		   for (int i=0;i<a.length;i++) {
			   if (a[i]==arg) {
				   return true;
			   }
		   }
		   return false;
	   }
	   private static int []duplicate(final int[]in){
		   final int[]value=new int[in.length];
		   for (int i=0;i<value.length;i++) {
			   value[i]=in[i];
		   }
		   return value;
	   }
	   
	   public static int []move(final int []selections, final Direction direction, final int lastIndex){
		   final int []oldLocations=duplicate(selections);
		   Arrays.sort(oldLocations);
		   return _move(oldLocations, direction, lastIndex);
	   }
	   
	   private static int []_move(final int []oldLocations, final Direction direction, final int lastIndex){
		   final int []newLocations;
		   switch(direction) {
		   case TOP:
			   newLocations=moveTop(oldLocations, 0);
			   break;
		   case BOTTOM:
			   newLocations=moveBottom(oldLocations, lastIndex);
			   break;
		   case UP:
			   newLocations=moveUp(oldLocations, 0);
			   break;
		   default:
			   newLocations=moveDown(oldLocations, lastIndex);
			   break;
		   }
		   return newLocations;
	   }
	   
	   public static Object[]move(final Object []in, final int []selections, final Direction direction){
		   final int []oldLocations=duplicate(selections);
		   Arrays.sort(oldLocations);
		   final int []newLocations=_move(oldLocations, direction, in.length-1);
		   final Object[]value=move(in, oldLocations, newLocations);
		   return value;
	   }
	   
	   private static int []moveUp(final int []input, final int firstIndex) {
		   final int []s=duplicate(input);
		   // s is sorted
		   int f=firstIndex;
		   for (int i=0;i<s.length;i++) {
			   if (s[i] != f) {
				   break;
			   }
			   f++;
		   }
		   for (int i=0;i<s.length;i++) {
			   if (s[i] > f) {
				   s[i]--;
			   }
		   }
		   return s;
	   }

	   private static int []moveDown(final int []input, final int lastIndex) {
		   final int []s=duplicate(input);
		   int l=lastIndex;
		   for (int i=s.length-1;i>=0;i--) {
			   if (s[i] != l) {
				   break;
			   }
			   l--;
		   }
		   for (int i=s.length-1;i>=0;i--) {
			   if (s[i] < l) {
				   s[i]++;
			   }
		   }
		   return s;
	   }
	   private static int []moveBottom(final int []input, final int bottomIndex) {
		   final int []s=duplicate(input);
		   
		   int j=0;
		   for (int i=s.length-1;i>=0;i--) {
			    s[i]=bottomIndex-j;
			   j++;
		   }
		   return s;
	   }
	   private static int []moveTop(final int []input, final int topIndex) {
		   final int []s=duplicate(input);
		   for (int i=0;i<input.length;i++) {
			    s[i]=topIndex+i;
			   
		   }
		   return s;
	   }
	   
	   static Object []move(final Object[] o, final int []oldLocations, final int[]newLocations) {
		   final Object []v=new Object[o.length];
		   for (int i=0;i<oldLocations.length;i++) {
			   v[ newLocations[i]]=o[ oldLocations[i]];
		   }
		   int j=0;
		   for (int i=0;i<o.length;i++) {
			   if (!contains(oldLocations, i)) {
			   while (v[j] != null) {
				   j++;
			   }
			   v[j]=o[i];
			   }
		   }
		   return v;
	   }
	   static void debug(final String t, final Object[]a) {
		   System.out.println(t);
		   int i=0;
		   for (final Object o:a) {
			   System.out.print(i++);
			   System.out.print("-");
			   System.out.print(Basics.toString(o));
			   System.out.print(" ");
			   if (i%9==0) {
				   System.out.println();
			   }
		   }
		   System.out.println();
		   System.out.println();
	   }
	   /**
	 * @param args
	 */
	public static void main(final String []args) {
		System.out.println("ere we go");
		   Object [] a= {/*0*/"killarney", /*1*/"pepper", /*2*/"deborah", /*3*/"brendan", /*4*/"connor", /*5*/"kieran", /*6*/"mark", 
				   /*7*/"heather", /*8*/"luke", /*9*/"gary", /*10*/"shelley", /*11*/"michelle", /*12*/"laura", /*13*/"emily", 
				   /*14*/"luke", /*15*/"sharon", /*16*/"david"};
		   debug("Starting state", a);
		   Object []o=move(a, new int[] {10, 8}, Direction.TOP);
		   debug("Move luke & shelley to top", o);
		   
		   o=move(a, new int[] {16, 0,1}, Direction.TOP);
		   debug("Move killarney, pepper & david to top", o);
		   
		   o=move(a, new int[] {10, 8}, Direction.BOTTOM);
		   debug("Move luke & shelley to bottom", o);
		   
		   o=move(a, new int[] {16, 0,1}, Direction.BOTTOM);
		   debug("Move killarney, pepper & david to top", o);
		   
		   o=move(a, new int[] {5,3}, Direction.DOWN);
		   debug("Move brendan & kieran down", o);
		   
		   o=move(a, new int[] {16, 13},Direction.DOWN);
		   debug("Move emily & david down", o);

		   o=move(a, new int[] {15, 16, 13},Direction.DOWN);
		   debug("Move emily, sharon & david down", o);

		   o=move(a, new int[] {2, 1},Direction.UP);
		   debug("Move deborah & pepper up ", o);
		   o=move(a, new int[] {0, 1,4},Direction.UP);
		   debug("Move killarny,pepper & connor up",o);
		   
	   }
}
