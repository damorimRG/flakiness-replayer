package samples;

import java.util.StringTokenizer;

public class Calculator {
    public int add( int x,  int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("arguments must be positive");
        }
        return x + y;
    }

    public int substract( int x,  int y) {
        return x - y;
    }

    public int multiply(int a, int b){
        int product = 0;
        int acopy = a;
        while (acopy < 0) {
            product = product - b;
            acopy = acopy + 1;
        }
        while (acopy > 0) {
            product = product + b;
            acopy = acopy - 1;
        }
        return product;
    }

    public int findMax(int arr[]){
        int max=0;
        for(int i=1;i<arr.length;i++){
            if(max<arr[i])
                max=arr[i];
        }
        return max;
    }
    public int cube(int n){
        return n*n*n;
    }

    public static String reverseWord(String str){

        StringBuilder result=new StringBuilder();
        StringTokenizer tokenizer=new StringTokenizer(str," ");

        while(tokenizer.hasMoreTokens()){
            StringBuilder sb=new StringBuilder();
            sb.append(tokenizer.nextToken());
            sb.reverse();

            result.append(sb);
            result.append(" ");
        }
        return result.toString();
    }
}