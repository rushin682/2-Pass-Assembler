/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c_compiler;

/**
 *
 * @author HP
 */
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

class MOT {

    public String mnemonic;
    public String opcode;
    public int length;
    public String oper_type;

    MOT(String mnemonic, String opcode, int length, String oper_type) {
        this.mnemonic = mnemonic;
        this.opcode = opcode;
        this.length = length;
        this.oper_type = oper_type;
    }
}

class SymbolTable {

    public String symbol;
    public String r_a;
    public int value;
    public int length;

    SymbolTable(String symbol, int value, int length, String r_a) {
        this.symbol = symbol;
        this.value = value;
        this.length = length;
        this.r_a = r_a;
    }
}

class LiteralTable {

    public String literal;
    public int value;
    public int length;
    public String r_a;

    LiteralTable(String literal, int value, int length, String r_a) {
        this.literal = literal;
        this.value = value;
        this.length = length;
        this.r_a = r_a;
    }
}

public class Pass2Assembler {

    public static ArrayList<MOT> mot_element;
    public static ArrayList<SymbolTable> sym_element;
    public static ArrayList<LiteralTable> lit_element;
    public static Map<Integer, Integer> BaseTable;
    public static ArrayList<String> POT;
    public static ArrayList<Integer> location_counter;
    public static ArrayList<String> literalList;
    public static PrintWriter pass1;
    public static PrintWriter pass2;
    public static int lc = 0;
    public static int row = 0;
    public static boolean litUpdate = false;
    public static String final_code;
    private static long startTime;
    private static long endTime;

    public static void main(String[] args) throws IOException {

        Pass2Assembler p2a = new Pass2Assembler();

        POT = new ArrayList<>();
        mot_element = new ArrayList<>();
        sym_element = new ArrayList<>();
        lit_element = new ArrayList<>();
        location_counter = new ArrayList<>();
        literalList = new ArrayList<>();
        BaseTable = new HashMap<>();

        mot_element.add(new MOT("LA", "01H", 4, "RX"));
        mot_element.add(new MOT("SR", "02H", 2, "RR"));
        mot_element.add(new MOT("L", "03H", 4, "RX"));
        mot_element.add(new MOT("AR", "04H", 2, "RR"));
        mot_element.add(new MOT("A", "05H", 4, "RX"));
        mot_element.add(new MOT("C", "06H", 4, "RX"));
        mot_element.add(new MOT("BNE", "07H", 4, "RX"));
        mot_element.add(new MOT("LR", "08H", 2, "RR"));
        mot_element.add(new MOT("ST", "09H", 4, "RX"));
        mot_element.add(new MOT("BR", "15H", 2, "RR"));

        POT.add("START");
        POT.add("END");
        POT.add("LTORG");
        POT.add("DC");
        POT.add("DS");
        POT.add("DROP");
        POT.add("USING");
        POT.add("EQU");
        startTime = System.nanoTime();
        System.out.println("Performing Pass1");
        p2a.pass1();
        System.out.println("Performing Pass2");
        p2a.pass2();
        endTime = System.nanoTime();
        long time=(endTime-startTime);
        System.out.println("");
        System.out.println("Total Time of Execution is:"+time);
    }

    private void pass1() throws IOException {

        pass1 = new PrintWriter(new FileWriter("ic.txt"), true);
        PrintWriter tables = new PrintWriter(new FileWriter("tables.txt"), true);

        Path path = FileSystems.getDefault().getPath("F:\\Git files\\2 Pass Assembler", "assembly.txt");

        try (InputStream in = Files.newInputStream(path);
                BufferedReader reader
                = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ArrayList<String> individual = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(line, " ", false);
                while (st.hasMoreTokens()) {
                    individual.add(st.nextToken());
                }
                /*for(int x=0;x<individual.size();x++){
                    System.out.print(individual.get(x)+"\t");
                }
                System.out.println("");*/
                if (searchPOT(individual) != 1) {
                    searchMOT(individual);

                }
                location_counter.add(lc);
                System.out.println(line);
                pass1.println(line);
            }
            tables.println("Symbol Table:\n Symbol\t\tValue\t\tLength\t\tR/A");
            System.out.println("Symbol Table:\n Symbol\t\tValue\t\tLength\t\tR/A");
            for (int i = 0; i < sym_element.size(); i++) {
                SymbolTable obj = sym_element.get(i);
                System.out.println(obj.symbol + "\t\t" + Integer.toString(obj.value) + "\t\t" + Integer.toString(obj.length) + "\t\t" + obj.r_a);
                tables.println(obj.symbol + "\t\t" + Integer.toString(obj.value) + "\t\t" + Integer.toString(obj.length) + "\t\t" + obj.r_a);
            }
            tables.println("");
            tables.println("Literal Table:\n Literal\t\tValue\t\tLength\t\tR/A");
            System.out.println("Literal Table:\n Literal\t\tValue\t\tLength\t\tR/A");
            for (int i = 0; i < lit_element.size(); i++) {
                LiteralTable obj_lit = lit_element.get(i);
                System.out.println(obj_lit.literal + "\t\t" + Integer.toString(obj_lit.value) + "\t\t" + Integer.toString(obj_lit.length) + "\t\t" + obj_lit.r_a);
                tables.println(obj_lit.literal + "\t\t" + Integer.toString(obj_lit.value) + "\t\t" + Integer.toString(obj_lit.length) + "\t\t" + obj_lit.r_a);
            }

        }
    }

    private int searchPOT(ArrayList<String> individual) {
        int i = 0;
        int l = 0;
        String temp = null;
        if (individual.size() == 3) {
            i = 1;
        }
        /* for(int x=0;x<individual.size();x++){
            System.out.print(individual.get(x)+"\t");
        }46767
        System.out.println("");*/
        individual = tokenizeOperands(individual);
        /*for(int test=0;test<individual.size();test++){
            System.out.print(individual.get(test)+"\t");
        }
        System.out.println("");*/
        String mnemonic = individual.get(i);
        if (mnemonic.equals("START")) {
            sym_element.add(new SymbolTable(individual.get(i - 1), Integer.parseInt(individual.get(i + 1)), 1, "R"));
            return 1;
        } else if (mnemonic.equals("DS")) {
            if (i == 1) {
                sym_element.add(new SymbolTable(individual.get(i - 1), lc, 4, "R"));
            }
            String operand = individual.get(i + 1);
            if (operand.endsWith("F")) {
                temp = operand.substring(0, (operand.length() - 1));
                l = 4 * Integer.parseInt(temp);
            } else if (operand.endsWith("H")) {
                temp = operand.substring(0, (operand.length() - 1));
                l = 2 * Integer.parseInt(temp);
            }
            lc = lc + l;
            return 1;
        } else if (mnemonic.equals("DC")) {
            if (i == 1) {
                sym_element.add(new SymbolTable(individual.get(i - 1), lc, 4, "R"));
            }
            String operand;
            for (int j = i + 1; j < individual.size(); j++) {
                operand = individual.get(j);
                if (operand.startsWith("F")) {
                    l = l + 4;
                } else if (operand.startsWith("H")) {
                    l = l + 2;
                }
            }
            lc = lc + l;
            return 1;
        } else if (mnemonic.equals("EQU")) {
            String number = individual.get(i + 1);
            if (!number.equals("*")) {
                sym_element.add(new SymbolTable(individual.get(i - 1), Integer.parseInt(number), 1, "A"));
            } else if (number.equals("*")) {
                sym_element.add(new SymbolTable(individual.get(i - 1), lc, 1, "R"));
            }
            return 1;
        } else if (mnemonic.equals("LTORG")) {
            //Yet to be written
            litUpdate = true;
            int length = 0;
            //System.out.println(literalList.size());
            for (int j = 0; j < literalList.size(); j++) {
                String literal = literalList.get(j);
                if (literal.startsWith("F")) {
                    length = 4;

                } else if (literal.startsWith("D")) {
                    length = 8;
                } else if (literal.startsWith("H")) {
                    length = 2;
                }
                lit_element.add(new LiteralTable(literal.substring(2, literal.length() - 1), lc, length, "R"));
                lc = lc + length;
            }
            return 1;

        } else if (mnemonic.equals("END")) {
            if (litUpdate == false) {
                int length = 0;
                //System.out.println(literalList.size());
                for (int j = 0; j < literalList.size(); j++) {
                    String literal = literalList.get(j);
                    if (literal.startsWith("F")) {
                        length = 4;

                    } else if (literal.startsWith("D")) {
                        length = 8;
                    } else if (literal.startsWith("H")) {
                        length = 2;
                    }
                    lit_element.add(new LiteralTable(literal.substring(2, literal.length() - 1), lc, length, "R"));
                    lc = lc + length;
                }
            }
            return 1;
        }

        return 0;
    }

    private void searchMOT(ArrayList<String> individual) {
        int i = 0;
        String str = null;
        if (individual.size() == 3) {
            i = 1;
        }
        /*for(int x=0;x<individual.size();x++){
            System.out.print(individual.get(x)+"\t");
        }
        System.out.println("");*/
        individual = tokenizeOperands(individual);
        /*for(int x=0;x<individual.size();x++){
            System.out.print(individual.get(x)+"\t");
        }
        System.out.println("");*/
        String temp = null;
        for (MOT mot_element1 : mot_element) {
            temp = individual.get(i);
            if (temp.equals(mot_element1.mnemonic)) {
                if (i == 1) {
                    sym_element.add(new SymbolTable(individual.get(i - 1), lc, mot_element1.length, "R"));
                }
                for (int j = i + 1; j < individual.size(); j++) {
                    str = individual.get(j);
                    if (str.startsWith("=")) {
                        str = str.substring(1, str.length());
                        literalList.add(str);
                    }
                }
                /*for(int xe=0;xe<literalList.size();xe++){
                    System.out.print(literalList.get(xe)+"\t");
                }
                System.out.println("");*/

                lc = lc + mot_element1.length;

            }

        }

    }

    private ArrayList tokenizeOperands(ArrayList<String> individual) {
        ArrayList<String> updated = new ArrayList<>();
        int l = individual.size();
        for (int i = 0; i < l - 1; i++) {
            updated.add(individual.get(i));
        }
        String last_word = individual.get(l - 1);
        //System.out.println(last_word);
        if (last_word.contains(",")) {
            StringTokenizer st = new StringTokenizer(last_word, ",", false);
            while (st.hasMoreTokens()) {
                updated.add(st.nextToken());
            }
            return updated;
        }
        return individual;
    }

    private void pass2() throws IOException {
        //System.out.println("\n\n\n\n\n\nYet to be done");
        pass2 = new PrintWriter(new FileWriter("pass2.txt"), true);
        PrintWriter tables = new PrintWriter(new FileWriter("tables_pass2.txt"), true);

        Path path = FileSystems.getDefault().getPath("F:\\Git files\\2 Pass Assembler", "ic.txt");
        try (InputStream in = Files.newInputStream(path);
                BufferedReader reader
                = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ArrayList<String> individual = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(line, " ", false);
                while (st.hasMoreTokens()) {
                    individual.add(st.nextToken());
                }
                /*for(int x=0;x<individual.size();x++){
                    System.out.print(individual.get(x)+"\t");
                }
                System.out.println("");*/
                if (searchPOT_2(individual) != 1) {
                    searchMOT_2(individual);

                }
                row++;

            }
            tables.println("Symbol Table(Pass2):\n Symbol\t\tValue\t\tLength\t\tR/A");
            System.out.println("Symbol Table(Pass2):\n Symbol\t\tValue\t\tLength\t\tR/A");
            for (int i = 0; i < sym_element.size(); i++) {
                SymbolTable obj = sym_element.get(i);
                System.out.println(obj.symbol + "\t\t" + Integer.toString(obj.value) + "\t\t" + Integer.toString(obj.length) + "\t\t" + obj.r_a);
                tables.println(obj.symbol + "\t\t" + Integer.toString(obj.value) + "\t\t" + Integer.toString(obj.length) + "\t\t" + obj.r_a);
            }
            tables.println("");
            tables.println("Literal Table(Pass2):\n Literal\t\tValue\t\tLength\t\tR/A");
            System.out.println("Literal Table(Pass2):\n Literal\t\tValue\t\tLength\t\tR/A");
            for (int i = 0; i < lit_element.size(); i++) {
                LiteralTable obj_lit = lit_element.get(i);
                System.out.println(obj_lit.literal + "\t\t" + Integer.toString(obj_lit.value) + "\t\t" + Integer.toString(obj_lit.length) + "\t\t" + obj_lit.r_a);
                tables.println(obj_lit.literal + "\t\t" + Integer.toString(obj_lit.value) + "\t\t" + Integer.toString(obj_lit.length) + "\t\t" + obj_lit.r_a);
            }

            tables.println("Base Table:\nBase Register\t\tValue");
            System.out.println("Base Table:\nBase Register\t\tValue");
            for (Map.Entry m : BaseTable.entrySet()) {
                System.out.println(m.getKey() + "\t\t" + m.getValue());
                tables.println(m.getKey() + "\t\t" + m.getValue());
            }

        }
    }

    private int searchPOT_2(ArrayList<String> individual) {
        int i = 0;
        int index = 0;
        int value = -1;
        if (individual.size() == 3) {
            i = 1;
        }

        individual = tokenizeOperands(individual);

        String mnemonic = individual.get(i);
        if (mnemonic.equals("USING")) {
            if (individual.get(i + 1).equals("*")) {
                index = individual.indexOf("*");
                individual.set(index, Integer.toString(location_counter.get(row)));
            } else {
                for (int j = i + 1; j < individual.size(); j++) {
                    String str = individual.get(j);
                    index = individual.indexOf(str);
                    for (int k = 0; k < sym_element.size(); k++) {
                        SymbolTable obj = sym_element.get(k);
                        if (str.equals(obj.symbol)) {
                            value = obj.value;
                            break;
                        }
                    }
                    individual.set(index, Integer.toString(value));
                }
                
            }
            BaseTable.put(new Integer(individual.get(i + 2)), new Integer(individual.get(i + 1)));
            String t = Integer.toString(location_counter.get(row)) + "\t";
            for (int s = 0; s < individual.size(); s++) {
                t = t + "  " + individual.get(s);
            }
            pass2.println(t);
            System.out.println(t);
            return 1;
        }

        return 0;
    }

    private void searchMOT_2(ArrayList<String> individual) {
        int i = 0;
        boolean flag = false;
        int index = 0;
        String operand = null;
        if (individual.size() == 3) {
            i = 1;
        }

        individual = tokenizeOperands(individual);
        int opertype=0;
        String temp = null;
        for (MOT mot_element1 : mot_element) {
            temp = individual.get(i);
            if (temp.equals(mot_element1.mnemonic)) {
                if((mot_element1.oper_type).equals("RX")){
                    opertype=1;
                }
                flag = true;
            }
        }
        if (flag == true) {
            if(opertype==1){
            for (int j = i + 1; j < individual.size(); j++) {
                operand = individual.get(j);
                if (searchSymbolTable(operand) == 1) {
                    for (SymbolTable sym_element1 : sym_element) {
                        if (operand.equals(sym_element1.symbol)) {
                            index = individual.indexOf(operand);
                            if(j==i+1){
                                individual.set(index, Integer.toString(sym_element1.value));
                            }
                            else{
                            String offset = offsetCreation(sym_element1.value);
                            individual.set(index, offset);
                            }
                        }
                    }

                } else if (searchLiteralTable(operand) == 1) {
                    for (LiteralTable lit_element1 : lit_element) {
                        String literal = lit_element1.literal;
                        if (operand.contains(literal)) {
                            index = individual.indexOf(operand);
                            if(j==i+1){
                                individual.set(index,(literal));
                            }
                            else{
                            String offset = offsetCreation(lit_element1.value);
                            individual.set(index, offset);
                            }
                        }

                    }
                }
            }
            }
            else if(opertype==0){
                 for (int j = i + 1; j < individual.size(); j++) {
                operand = individual.get(j);
                if (searchSymbolTable(operand) == 1) {
                    for (SymbolTable sym_element1 : sym_element) {
                        if (operand.equals(sym_element1.symbol)) {
                            index = individual.indexOf(operand);
                            //String offset = offsetCreation(sym_element1.value);
                            individual.set(index, Integer.toString(sym_element1.value));
                        }
                    }

                } else if (searchLiteralTable(operand) == 1) {
                    for (LiteralTable lit_element1 : lit_element) {
                        String literal = lit_element1.literal;
                        if (operand.contains(literal)) {
                            index = individual.indexOf(operand);
                            //String offset = offsetCreation(lit_element1.value);
                            //System.out.println("/////////////////////"+offset+"///////////////////");
                            individual.set(index, literal);
                        }

                    }
                }
            }
            }
        }
        String t = Integer.toString(location_counter.get(row)) + "\t";
        for (int s = 0; s < individual.size(); s++) {
            t = t + "  " + individual.get(s);
        }
        pass2.println(t);
        System.out.println(t);

    }

    private String offsetCreation(int value) {
        String Total;
        if (BaseTable.size() > 1) {
            int min = 100;
            String base = null;
            for (Map.Entry m : BaseTable.entrySet()) {
                int check = Integer.valueOf(m.getValue().toString());
                if (min > Math.abs(check - value)) {
                    min = Math.abs(check - value);
                    base = m.getKey().toString();
                }
            }
            Total = Integer.toString(min) + "(0, " + base + ")";
            return Total;
        }
        Map.Entry<Integer, Integer> entry = BaseTable.entrySet().iterator().next();
        String key = Integer.toString(entry.getKey());
        int wer = entry.getValue();

        String basic = Integer.toString(Math.abs(wer - value)) + "(0, " + key + ")";
        return basic;
    }

    private int searchSymbolTable(String operand) {
        for (SymbolTable sym_element1 : sym_element) {
            if (operand.equals(sym_element1.symbol)) {
                return 1;
            }
        }
        return 0;
    }

    private int searchLiteralTable(String operand) {
        for (LiteralTable lit_element1 : lit_element) {
            String str = lit_element1.literal;
            if (operand.contains(str)) {
                return 1;
            }
        }
        return 0;
    }
}
