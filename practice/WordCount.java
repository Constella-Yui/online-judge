import java.nio.file.*;
import java.util.*;

public class WordCount{
    public static void main(String[] args)throws Exception{
        String text=Files.readString(Path.of(args[0]));
        //System.out.println(text.length());
        String[] words=text.toLowerCase().split("\\W+");
        //for(String s:words)System.out.println(s);
        Map<String,Integer>freq=new HashMap<>();
        for(String w:words){
            if(w.isEmpty())continue;
            freq.merge(w,1,Integer::sum);
        }
        List<Map.Entry<String,Integer>>list=new ArrayList<>(freq.entrySet());
        list.sort((a,b)->{
            int byCount=b.getValue()-a.getValue();
            if(byCount!=0)return byCount;
            return a.getKey().compareTo(b.getKey());
        });
        for(int i=0;i<Math.min(10,list.size());i++){
            Map.Entry<String,Integer>e=list.get(i);
            System.out.println(e.getKey()+" "+e.getValue());
        }
    }
}