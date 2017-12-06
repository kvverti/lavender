package kvverti.lavender.operators;

import java.io.*;

import kvverti.lavender.Stack;
        
public class FileObj extends Operator implements Closeable {
    
    private File file;
    private OutputStream out;
    private InputStream in;
    
    public FileObj(boolean sysin) {
        
        super(":$FILE", 0, 1, Operator.NA);
        file = null;
        if(sysin) {
            out = null;
            in = System.in;
        } else {
            out = System.out;
            in = null;
        }
    }
    
    public FileObj(String name, String args) {
        
        super(":$FILE", 0, 1, Operator.NA);
        file = new File(name);
        if(args.contains("r"))
            try {
                in = new FileInputStream(file);
            } catch(FileNotFoundException e) {
                in = null;
            }
        else
            in = null;
        if(args.contains("w"))
            try {
                out = new FileOutputStream(file);
            } catch(FileNotFoundException e) {
                out = null;
            }
        else
            out = null;
    }
    
    public void write(String s) {
        
        if(out == null)
            return;
        for(int i = 0; i < s.length(); i++)
            try {
                out.write(s.charAt(i));
            } catch(IOException e) {
                return;
            }
    }
    
    public int read() {
        
        if(in == null)
            return -1;
        try {
            return in.read();
        } catch(IOException e) {
            return -1;
        }
    }
    
    public String read(int len) {
        
        if(in == null)
            return "";
        //if len > 0, read in len bytes (or till end of file)
        if(len > 0) {
            byte[] b = new byte[len];
            int num; //actual length
            try {
                num = in.read(b);
            } catch(IOException e) {
                return "";
            }
            StringBuilder sb = new StringBuilder(num);
            for(int i = 0; i < num; i++)
                sb.append((char)(b[i] & 0xff));
            return sb.toString();
        } else if(len == 0) {
            //read the entire file and return as a string
            byte[] b = new byte[512];
            StringBuilder sb = new StringBuilder(512);
            int num;
            try {
                num = in.read(b);
                while(num >= 0) {
                    for(int i = 0; i < num; i++)
                        sb.append((char)(b[i] & 0xff));
                    num = in.read(b);
                }
            } catch(IOException e) {
                return "";
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    
    @Override
    public void eval(Operator[] p, Stack stack) {
        
        stack.push(this);
    }
    
    @Override
    public void close() {
        
        //built in stream
        if(file == null)
            return;
        try {
            if(out != null)
                out.close();
        } catch(IOException e) { }
        try {
            if(in != null)
                in.close();
        } catch(IOException e) { }
        System.out.println("close");
    }
}