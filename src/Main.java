import java.io.*;
import java.util.ArrayList;

/**
 * Data File Creation   : java Main -c index.dat [degree]
 * Insertion            : java Main -i index.dat input.csv
 * Deletion             : java Main -d index.dat delete.csv
 * Single Key Search    : java Main -s index.dat [key]
 * Ranged Search        : java Main -r index.dat [start] [end]
 */


public class Main {

    static class Node {
        /* constructor of Node */
        Node(){
            this.p = new ArrayList<>();
            this.r = null;
            this.parent = null;
            this.m = 0;
        }

        /* member variables */
        int m;
        ArrayList<Pair> p;
        Node r;
        Node parent;

        /* member class Pair */
        static class Pair{
            int key;
            int value;
            Node child;

            Pair(int key, int value){ // for leaf node
                this.key = key;
                this.value = value;
                this.child = null;
            }
            Pair(int key, int value, Node child){ // for index node
                this(key, value);
                this.child = child;
            }
            Pair(Pair p){
                this(p.key, p.value, p.child);
            }
        }
    }

    private static Node root = null;
    private static int degree;

    private static int count = 0; // tree index for writeTree
    private static String leaves; // temporary Buffer for writeTree

    public static void main(String[] args) {

        if(args == null){
            System.out.println("No argument!");
            return;
        }

        switch (args[0])
        {
            case "-c":
                if(args.length < 3){
                    System.out.println("Wrong argument!");
                    break;
                }
                creation(args[1], Integer.parseInt(args[2]));
                break;


            case "-i":
                if(args.length < 3){
                    System.out.println("Wrong argument!");
                    break;
                }
                insertion(args[1], args[2]);
                break;

            case "-d":
                if(args.length < 3){
                    System.out.println("Wrong argument!");
                    break;
                }
                deletion(args[1], args[2]);
                break;

            case "-s":
                if(args.length < 3){
                    System.out.println("Wrong argument!");
                    break;
                }
                singleKeySearch(args[1], Integer.parseInt(args[2]));
                break;

            case "-r":
                if(args.length < 4){
                    System.out.println("Wrong argument!");
                    break;
                }
                rangedSearch(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                break;

            default:
                System.out.println("Command error! Check Command!");
        }
    }

    private static void creation(String indexFileName, int d){
        degree = d;
        try {
            File indexFile = new File("../build/" + indexFileName);
            FileWriter fw = new FileWriter(indexFile);
            fw.write("$ " + degree + "\n");
            fw.flush();
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void insertion(String indexFileName, String inputFileName){
        readTree(indexFileName);

        try {
            File inputFile = new File("../build/" + inputFileName);
            FileReader fr = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fr);

            String tmp;
            while((tmp = br.readLine()) != null){
                String[] keyNvalue = tmp.split(",");
                addLeaf(Integer.parseInt(keyNvalue[0]), Integer.parseInt(keyNvalue[1]));
            }

        }catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("insertion done!");

        writeTree(indexFileName);
    }

    private static void addLeaf(int key, int value){
        // first insertion
        if(root == null){
            root = new Node();
            root.p.add(new Node.Pair(key, value)); root.m++;
            return;
        }

        /* go down to leaf node */
        Node P = root;
        while(true){
            /* check if it's leaf node*/
            if(P.p.get(0).child == null)
                break;
            else{
                if((P.p.get(P.p.size()-1).key) < key){
                    P = P.r;
                    continue;
                }
                for(int i = 0; i < P.p.size(); i++){
                    if(key < P.p.get(i).key){
                        P = P.p.get(i).child;
                        break;
                    }
                    else if(key == P.p.get(i).key){
                        if(i+1 < P.p.size())
                            P = P.p.get(i+1).child;
                        else P = P.r;
                    }
                }
            }
        }

        /* P == LEAF! insert key to LEAF node */
        if((P.p.get(P.p.size()-1).key) < key)
            P.p.add(new Node.Pair(key, value)); P.m++;
        for(int i = 0; i<P.p.size(); i++){
            if(key < P.p.get(i).key){
                P.p.add(i, new Node.Pair(key, value)); P.m++;
                break;
            }
        }

        /* Did LEAF node overflow ? */
        if(P.p.size() < degree) { // if not,
            return;
        }
        // if overflowed, split it
        Node left = P;
        Node right = new Node();
        Node.Pair up = new Node.Pair(P.p.get(degree/2));

        for(int i = degree/2; i < degree; i++){
            right.p.add(new Node.Pair(left.p.get(i))); right.m++;
        }
        for(int i = degree-1; i >= degree/2; i--){
            left.p.remove(i); left.m--;
        }
        up.child = left;
        right.r = P.r;
        left.r = right;

        /* if overflowed LEAF is a root */
        if(P == root){
            root = new Node();
            root.p.add(up); root.m++;

            // whose child RIGHT is?
            root.r = right;

            left.parent = root;
            right.parent = root;

            return;
        }
        /* if this LEAF is NOT a root */
        else{
            Node parent = P.parent;

            /* merge UP to the parent Node */
            if(parent.p.get(parent.p.size()-1).key < up.key) {
                parent.p.add(up); parent.m++;
                // whose child RIGHT is?
                parent.r = right;
            }
            for(int i = 0; i<parent.p.size(); i++){
                if(up.key < parent.p.get(i).key){ // inserting into [i] of parent
                    // now UP is parent[i]
                    parent.p.add(i, up); parent.m++;
                    // whose child RIGHT is?
                    parent.p.get(i + 1).child = right;

                    break;
                }
            }
            left.parent = P.parent;
            right.parent = P.parent;
        }
        // done.
        if(P.parent.p.size() < degree) {
            return;
        }

        /* if INDEX node overflowed?! */
        P = P.parent;
        while(true){
            if(P.p.size() < degree) {
                return;
            }

            /* split it */
            left = P;
            right = new Node();
            up = new Node.Pair(P.p.get(degree/2));

            for(int i = degree/2 + 1; i < degree; i++){
                right.p.add(new Node.Pair(left.p.get(i))); right.m++;
            }
            for(int i = degree-1; i >= degree/2; i--){
                left.p.remove(i); left.m--;
            }
            right.r = P.r;
            left.r = up.child;
            up.child = left;

            right.r.parent = right; // right should be her child's parent
            for(Node.Pair kv : right.p){
                kv.child.parent = right;
            }

            /* if this INDEX node is a root */
            if(P == root){
                root = new Node();
                root.p.add(up); root.m++;

                // whose child RIGHT is?
                root.r = right;

                left.parent = root;
                right.parent = root;

                return;
            }
            /* if INDEX node is NOT a root */
            else{
                Node parent = P.parent;
                /* merge UP to its parent node */
                if(parent.p.get(parent.p.size()-1).key < up.key) {
                    parent.p.add(up); parent.m++;

                    // whose child RIGHT is?
                    parent.r = right;
                }
                for(int i = 0; i<parent.p.size(); i++){
                    if(up.key < parent.p.get(i).key){ // inserting into [i] of parent
                        // now UP is parent[i]
                        parent.p.add(i, up); parent.m++;
                        // whose child RIGHT is?
                        parent.p.get(i + 1).child = right;

                        break;
                    }
                }

                left.parent = P.parent;
                right.parent = P.parent;
            }

            // check Parent which is merged overflowed
            P = P.parent;
        }
    }

    private static void readTree(String indexFileName){
        try {
            File indexFile = new File("../build/" + indexFileName);
            FileReader fr = new FileReader(indexFile);
            BufferedReader br = new BufferedReader(fr);

            /* tempTree */
            ArrayList<Node> tempTree = new ArrayList<>();
            tempTree.add(new Node()); // [0] is empty node

            /* read tree info from the index file */
            String indexLine;
            while((indexLine = br.readLine()) != null){
                String[] nodeInfo = indexLine.split(" ");

                /* node insert */
                switch (nodeInfo[0]) {

                    case "$": /* get degree */
                        degree = Integer.parseInt(nodeInfo[1]);
                        System.out.println("degree : " + degree);
                        break;


                    case "#":
                        /* save all the key-value pairs into temp node */
                        Node tempNode = new Node();
                        for (int i = 3; i < nodeInfo.length; i += 2) {
                            if (i + 1 < nodeInfo.length) {
                                int key = Integer.parseInt(nodeInfo[i]);
                                int value = Integer.parseInt(nodeInfo[i + 1]);

                                tempNode.p.add(new Node.Pair(key, value));
                                tempNode.m++;
                            }
                        }
                        /* save temp nodes to tempTree and connect them */
                        tempTree.add(tempNode);
                        Node childNode = tempTree.get(Integer.parseInt(nodeInfo[1]));
                        Node parentNode = tempTree.get(Integer.parseInt(nodeInfo[2]));

                        /* root node has no parent */
                        if (nodeInfo[2].equals("0"))
                            break; // escape switch

                        /* connect tempTree */
                        for (Node.Pair p : parentNode.p) {
                            if (childNode.p.get(0).key < p.key) {
                                p.child = childNode;
                                childNode.parent = parentNode;
                                break;
                            }
                        }
                        if (childNode.parent == null) {
                            childNode.parent = parentNode;
                            parentNode.r = childNode;
                        }
                        break;


                    case "&": /* connect LEAF nodes each other */
                        for (int i = 1; i <= nodeInfo.length - 2; i++) {
                            int node1 = Integer.parseInt(nodeInfo[i]);
                            int node2 = Integer.parseInt(nodeInfo[i + 1]);
                            tempTree.get(node1).r = tempTree.get(node2);
                        }
                        break;
                }
            }

            /* root = tempTree[1] */
            if(tempTree.size() >= 2)
                root = tempTree.get(1);
            else root = null;

            System.out.println("readTree done!");
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void writeTree(String indexFileName){
        try {
            File indexFile = new File("../build/" + indexFileName);
            FileWriter fw = new FileWriter(indexFile);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("$ " + degree + "\n");
            leaves = "&";

            writeNode(bw, root, 0);

            bw.write(leaves);

            bw.flush();
            bw.close();

            System.out.println("writeTree done!");

            printAllKeys();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void writeNode(BufferedWriter bw, Node node, int parent){
        try {
            /* write down the node info */
            int myIndex = ++count;
            bw.write("# " + myIndex + " " + parent);
            for (Node.Pair i : node.p) {
                bw.write(" " + i.key + " " + i.value);
            }
            bw.write("\n");

            /* if this node is LEAF */
            if(node.p.get(0).child == null) {
                leaves += " " + myIndex;
                return;
            }

            for(Node.Pair i : node.p){
                if(i.child != null)
                    writeNode(bw, i.child, myIndex);
            }
            if(node.r != null)
                writeNode(bw, node.r, myIndex);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void singleKeySearch(String indexFileName, int key){

        readTree(indexFileName);

        Node N = root;
        if(N == null) {
            System.out.println("Tree doesn't exist!");
            return;
        }

        while(true){
            /* if it's leaf node, */
            if(N.p.get(0).child == null) {
                for(Node.Pair p : N.p){
                    if(p.key == key){
                        System.out.println(p.value);
                        System.out.println("singleKeySearch done!");
                        return;
                    }
                }
                System.out.println("NOT FOUND");
                System.out.println("singleKeySearch done!");
                return;
            }

            else{ // if not,
                // print keys of index node
                for(Node.Pair p : N.p){
                    System.out.print(p.key + " ");
                }
                System.out.println();

                // and then move to leaf
                if((N.p.get(N.p.size()-1).key) <= key){
                    N = N.r;
                    continue;
                }
                for(int i = 0; i < N.p.size(); i++){
                    if(key < N.p.get(i).key){
                        N = N.p.get(i).child;
                        break;
                    }
                    else if(key == N.p.get(i).key){
                        if(i+1 < N.p.size())
                            N = N.p.get(i+1).child;
                        else N = N.r;
                    }
                }
            }
        }
    }

    private static void rangedSearch(String indexFileName, int key1, int key2){
        readTree(indexFileName);

        Node N = root;
        if(N == null) {
            System.out.println("Tree doesn't exist!");
            return;
        }

        while(true){
            /* if it's leaf node, */
            if(N.p.get(0).child == null) {
                break;
            }

            else{ // if not,
                // move to leaf
                if((N.p.get(N.p.size()-1).key) <= key1){
                    N = N.r;
                    continue;
                }
                for(int i = 0; i < N.p.size(); i++){
                    if(key1 < N.p.get(i).key){
                        N = N.p.get(i).child;
                        break;
                    }
                    else if(key1 == N.p.get(i).key){
                        if(i+1 < N.p.size())
                            N = N.p.get(i+1).child;
                        else N = N.r;
                    }
                }
            }
        }

        // key1 can be inserted to the node N
        boolean searchSuccess = false;
        leaves:
        while(true){
            for(Node.Pair p : N.p){
                if(p.key < key1) continue;
                else if(key1 <= p.key && p.key <= key2){
                    System.out.println(p.key + ", " + p.value);
                    searchSuccess = true;
                }
                else if(key2 < p.key) break leaves;
            }

            if(N.r == null) break;
            N = N.r;
        }

        if(!searchSuccess) {
            System.out.println("NOT FOUND");
        }

        System.out.println("rangedSearch done!");
    }

    private static void deletion(String indexFileName, String deleteFileName){
        readTree(indexFileName);

        try {
            File deleteFile = new File("../build/" + deleteFileName);
            FileReader fr = new FileReader(deleteFile);
            BufferedReader br = new BufferedReader(fr);

            String key;
            while((key = br.readLine()) != null){
                deleteLeaf(Integer.parseInt(key));
            }
            System.out.println("deletion done!");

        }catch(IOException e){
            e.printStackTrace();
        }

        writeTree(indexFileName);
    }

    private static void deleteLeaf(int key){

        // go down to LEAF
        Node problem = root;
        while(true){
            /* check if it's leaf node*/
            if(problem.p.get(0).child == null)
                break;
            else{
                if((problem.p.get(problem.p.size()-1).key) < key){
                    problem = problem.r;
                    continue;
                }
                for(int i = 0; i < problem.p.size(); i++){
                    if(key < problem.p.get(i).key){
                        problem = problem.p.get(i).child;
                        break;
                    }
                    else if(key == problem.p.get(i).key){
                        if(i+1 < problem.p.size())
                            problem = problem.p.get(i+1).child;
                        else problem = problem.r;
                    }
                }
            }
        }

        // if there is key, remove it.
        boolean isThereKey = false;
        for(Node.Pair kv : problem.p){
            if(kv.key == key){
                isThereKey = true;
                problem.p.remove(kv);
                break;
            }
        }
        // else deletion end
        if(!isThereKey) {
            System.out.println("No such key! Deletion failed!");
            return;
        }

        fixUnderFlow(problem);

        System.out.println("deleteLeaf done! key : " + key);
        System.out.println();
    }

    private static void fixUnderFlow(Node problem){
        if(problem == null) {
            System.out.println("error! problem is null!");
            return;
        }

        int minKeyNum;
        if(degree%2 == 1)   minKeyNum = degree/2; //odd
        else                minKeyNum = degree/2 -1;//even

        // check if it is underflow
        if(problem.p.size() >= minKeyNum) return;
        else if(problem == root){
            if(problem.p.size() == 0) {
                root = problem.r;
                System.out.println("ROOT fixed!");
            }
            return;
        }

        Node parent = problem.parent;

        //check if problem is LEAF
        boolean leaf = false;
        if(problem.r == null)
            leaf = true; //problem has no child
        if(parent.r.p.size() != 0 && parent.r.p.get(0).child == null)
            leaf = true; // sibling has no child
        for(Node.Pair kv : parent.p){
            if(kv.child.p.size() != 0 && kv.child.p.get(0).child == null) {
                leaf = true;
                break;
            }
        }

        // if problem is a LEAF node
        if(leaf){
            Node.Pair i = null; //parent pair i
            Node.Pair i_ = null; //parent's left pair i-1
            Node.Pair i__ = null; //parent's right pair i+1

            if(problem.p.size() > 0) { //degree is over 5
                if (parent.p.get(parent.p.size() - 1).key <= problem.p.get(0).key) {
                    i_ = parent.p.get(parent.p.size() - 1);
                }
                for (int j = 0; j < parent.p.size(); j++) {
                    if (problem.p.get(0).key < parent.p.get(j).key) {
                        i = parent.p.get(j);
                        if (j - 1 >= 0) i_ = parent.p.get(j - 1);
                        if (j + 1 < parent.p.size()) i__ = parent.p.get(j + 1);
                        break;
                    }
                }
            }
            else{ //degree is 3 or 4
                if (parent.r.p.size() == 0){ //problem == parent.r
                    i_ = parent.p.get(parent.p.size() - 1);
                }
                for(int j = 0; j < parent.p.size(); j++){
                    if(parent.p.get(j).child.p.size() == 0){
                        i = parent.p.get(j);
                        if (j - 1 >= 0) i_ = parent.p.get(j - 1);
                        if (j + 1 < parent.p.size()) i__ = parent.p.get(j + 1);
                        break;
                    }
                }
            }

            // BORROW from RIGHT sibling (leaf ver.)
            if(i != null){

                Node right;
                if(i__ != null) right = i__.child;
                else right = parent.r;

                if(right.p.size() > minKeyNum) {
                    Node.Pair borrow = new Node.Pair(right.p.get(0));
                    problem.p.add(borrow); problem.m++;
                    i.key = right.p.get(1).key;
                    i.value = right.p.get(1).value;
                    right.p.remove(0); right.m--;

                    System.out.println("##### LEAF : BORROW from RIGHT #####");
                }
            }
            // BORROW from LEFT sibling (leaf ver.)
            else if (i_ != null && i_.child.p.size() > minKeyNum) {
                // i == null? no no
                Node left = i_.child;
                Node.Pair borrow = new Node.Pair(left.p.get(left.p.size()-1));
                problem.p.add(0, borrow); problem.m++;
                i_.key = borrow.key;
                i_.value = borrow.value;
                left.p.remove(left.p.size()-1); left.m--;

                System.out.println("##### LEAF : BORROW from LEFT #####");
            }

            // if we cannot borrow, MERGE
            if(problem.p.size() < minKeyNum){
                // MERGE with LEFT sibling (using i, i-1)
                if(i_ != null) {
                    Node left = i_.child;

                    for(Node.Pair kv : problem.p){
                        left.p.add(new Node.Pair(kv)); left.m++;
                    }
                    left.r = problem.r;
                    if(i!=null) i.child = left;
                    else parent.r = left;
                    parent.p.remove(i_); parent.m--;

                    System.out.println("##### LEAF : MERGE with LEFT #####");
                }
                //MERGE with RIGHT sibling (using i, i+1)
                else{
                    Node right;
                    if(i__ != null) right = i__.child;
                    else right = parent.r;

                    for(Node.Pair kv : right.p){
                        problem.p.add(new Node.Pair(kv)); problem.m++;
                    }
                    problem.r = right.r;

                    if(i__ != null) i__.child = problem;
                    else parent.r = problem;
                    parent.p.remove(i);

                    System.out.println("##### LEAF : MERGE with RIGHT #####");
                }
            }
        }
        // if problem is a INDEX node
        else{
            Node.Pair i = null; //parent pair i
            Node.Pair i_ = null; //parent's left pair i-1
            Node.Pair i__ = null; //parent's right pair i+1

            if(problem.p.size() > 0) { // degree is over 5
                if (parent.p.get(parent.p.size() - 1).key < problem.p.get(0).key) {
                    i_ = parent.p.get(parent.p.size() - 1);
                }
                for (int j = 0; j < parent.p.size(); j++) {
                    if (problem.p.get(0).key < parent.p.get(j).key) {
                        i = parent.p.get(j);
                        if (j - 1 >= 0) i_ = parent.p.get(j - 1);
                        if (j + 1 < parent.p.size()) i__ = parent.p.get(j + 1);
                        break;
                    }
                }
            }
            else{ //degree is 3 or 4
                if (parent.r.p.size() == 0){
                    i_ = parent.p.get(parent.p.size() - 1);
                }
                for(int j = 0; j < parent.p.size(); j++){
                    if(parent.p.get(j).child.p.size() == 0){
                        i = parent.p.get(j);
                        if (j - 1 >= 0) i_ = parent.p.get(j - 1);
                        if (j + 1 < parent.p.size()) i__ = parent.p.get(j + 1);
                        break;
                    }
                }
            }

            //BORROW from RIGHT sibling (index ver.)
            if(i != null){
                Node right;
                if(i__ != null) right = i__.child;
                else right = parent.r;

                if(right.p.size() > minKeyNum){
                    Node.Pair down = new Node.Pair(i);
                    problem.p.add(down);
                    down.child = problem.r;
                    problem.r = right.p.get(0).child;
                    i.key = right.p.get(0).key;
                    i.value = right.p.get(0).value;
                    right.p.remove(0);

                    System.out.println("##### INDEX : BORROW from RIGHT #####");
                }
            }
            //BORROW from LEFT sibling (index ver.)
            else if(i_ != null && i_.child.p.size() > minKeyNum){
                Node left = i_.child;
                Node.Pair down = new Node.Pair(i_);
                down.child = left.r;
                left.r = left.p.get(left.p.size()-1).child;
                i_.key = left.p.get(left.p.size()-1).key;
                i_.value = left.p.get(left.p.size()-1).value;
                left.p.remove(left.p.size()-1); left.m--;

                System.out.println("##### INDEX : BORROW from LEFT #####");
            }

            // if we cannot borrow, MERGE
            if(problem.p.size() < minKeyNum) {
                //MERGE with RIGHT sibling (index ver.)
                if (i != null) {
                    Node right;
                    if (i__ != null) right = i__.child;
                    else right = parent.r;

                    Node.Pair down = new Node.Pair(i);
                    problem.p.add(down); problem.m++;
                    for (Node.Pair kv : right.p) {
                        problem.p.add(new Node.Pair(kv)); problem.m++;
                    }

                    down.child = problem.r;
                    problem.r = right.r;
                    if (i__ != null) i__.child = problem;
                    else parent.r = problem;
                    parent.p.remove(i); parent.m--;

                    // set child's parent : problem
                    problem.r.parent = problem;
                    for (Node.Pair kv : problem.p) {
                        kv.child.parent = problem;
                    }

                    System.out.println("##### INDEX : MERGE with RIGHT #####");
                }
                //MERGE with LEFT sibling (index ver.)
                else if (i_ != null) {
                    Node left = i_.child;
                    Node.Pair down = new Node.Pair(i_);

                    left.p.add(down);
                    down.child = left.r;
                    left.r = problem.r;
                    for (Node.Pair kv : problem.p) { // is_it_okay...?
                        left.p.add(new Node.Pair(kv));
                    }
                    parent.r = left; // for no i+1
                    parent.p.remove(i_);

                    // set child's parent : problem
                    left.r.parent = left;
                    for (Node.Pair kv : left.p) {
                        kv.child.parent = left;
                    }

                    System.out.println("##### INDEX : MERGE with LEFT #####");
                }
            }
        }

        fixUnderFlow(parent);
    }

    private static void printAllKeys(){
        try {
            File output = new File("../build/allKeys.dat");
            FileWriter fw = new FileWriter(output);

            // go down to the first key
            Node print = root;
            while(print != null){
                if(print.p.get(0).child == null){
                    break;
                }
                print = print.p.get(0).child;
            }

            // print all the keys
            int numOfKey = 0;
            while(print != null){
                for(Node.Pair kv : print.p){
                    ++numOfKey;
                    fw.write(kv.key + " ");

                    if(numOfKey % 10 == 0)
                        fw.write("\n");
                }
                print = print.r;
            }

            fw.flush();
            fw.close();
            System.out.println("printAllKeys done!");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}