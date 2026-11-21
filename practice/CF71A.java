import java.io.*;
import java.util.*;

public class CF71A{
    public static void main(String[] args)throws IOException{
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        int n=Integer.parseInt(br.readLine().trim());
        for(int i=0;i<n;i++){
            String s=br.readLine().trim();
            StringBuilder ans=new StringBuilder("");
            int m=s.length();
            if(m>10){
                int cnt=m-2;
                ans.append(s.charAt(0));
                ans.append(cnt);
                ans.append(s.charAt(m-1));
            }
            else{
                for(int j=0;j<m;j++){
                    ans.append(s.charAt(j));
                }
            }
            System.out.println(ans);
        }

    }
}