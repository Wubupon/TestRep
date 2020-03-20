import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.TextField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

class PostfixToInfix extends JFrame {
    private TextField input, output;
    private JPanel rightPanelTop, leftPanelTop, middlePanel, bottomLeftPanel, bottomRightPanel;
    private JButton evaluate;
    private JLabel enter, result;

    private PostfixToInfix() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.setTextFields();
        this.setEvaluate();
        this.setLabels();
        this.setPanels();
        this.addPanels();
        this.setTitle("Three Address Generator");
        this.setPreferredSize(new Dimension(325, 150));
        this.setSize(this.getPreferredSize());
        this.validate();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }

    // abstract so i can just call the method in the constructor.
    private void setTextFields() {
        this.input = new TextField("", 15);
        this.output = new TextField("", 15);
        this.output.setEditable(false);
    }

    private void setLabels() {
        this.enter = new JLabel("Enter Postfix Expression");
        this.result = new JLabel("Infix Expression");
    }

    // abstract so i can just call the method in the constructor.
    // also, my attempt to make the gui look pretty
    private void setPanels() {
        this.leftPanelTop = new JPanel();
        this.leftPanelTop.setLayout(new GridLayout(1, 1));
        this.leftPanelTop.add(this.enter);
        this.rightPanelTop = new JPanel();
        this.rightPanelTop.setLayout(new GridLayout(1, 1));
        this.rightPanelTop.add(this.input);
        this.middlePanel = new JPanel();
        this.middlePanel.setLayout(new GridLayout(1, 1));
        this.middlePanel.add(this.evaluate);
        this.bottomLeftPanel = new JPanel();
        this.bottomLeftPanel.setLayout(new GridLayout(1, 1));
        this.bottomLeftPanel.add(this.result);
        this.bottomRightPanel = new JPanel();
        this.bottomRightPanel.setLayout(new GridLayout(1, 1));
        this.bottomRightPanel.add(this.output);
    }

    private void addPanels() {
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(1, 2));
        top.add(this.leftPanelTop);
        top.add(this.rightPanelTop);
        this.add(top);
        JPanel middle = new JPanel(new GridLayout(1, 1));
        middle.add(this.middlePanel);
        this.add(middle);
        JPanel bottom = new JPanel();
        bottom.setLayout(new GridLayout(1, 2));
        bottom.add(this.bottomLeftPanel);
        bottom.add(this.bottomRightPanel);
        this.add(bottom);
    }

    // abstract so i can just call the method in the constructor.
    // This one is important.
    // it sets the action listener, which will perform the infix expression evaluation
    private void setEvaluate() {
        this.evaluate = new JButton("Construct Tree");
        this.evaluate.addActionListener(e -> this.performExpr());
    }

    // method called when you click evaluate
    private void performExpr() {
        try {
            this.output.setText(new Expr(this.input.getText()).doMath());
        } catch (Except ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    // one liner main
    public static void main(String[] args) {
        new PostfixToInfix();
    }

}

@SuppressWarnings("ALL")
class Expr extends Leaf {

    //vars
    private String[] cleanString;
    private Stack<String> cleanStack;
    private ArrayList<String> arith = new ArrayList<>(); // just a handy way to check if token is +,/,+ or -
    private ArrayList<Leaf> leaves = new ArrayList<>(); // keep track of my "expression tree"
    private ArrayList<String> result = new ArrayList<>(); // to print out the infix
    private Leaf root;

    // constructor for base Expr, used by main class
    Expr(String str) {
        this.cleanStack = new Stack<>();
        this.cleanString = this.clean(str);
        this.arith.add("+");
        this.arith.add("-");
        this.arith.add("*");
        this.arith.add("/");
        this.root = null;
    }

    // constructor that passes of the handle to a child leaf
    // honestly no good reason to be using an abstract class here that i can think of
    Expr(String token, Leaf parent) {
        super(token, parent);
    }

    private String[] clean(String str) {
        return str.replaceAll("\\s+", "").split("");
    }

    public String doMath() throws Except {
        for (String st : this.cleanString) {
            if (!st.matches("\\d+|\\+|-|/|\\*")) { // here's a regex to check for illegal characters
                this.cleanString = new String[]{""}; // clear out cleanString if it's bad
                throw new Except("Invalid token " + st); // Throw the error
            }
        }
        if (this.cleanString.length > 1) {
            for (String token : this.cleanString) {
                this.cleanStack.push(token); // using a stack because reasons
            }
            this.traverse(this.cleanStack.pop());
            this.populateResults();
        }
        return this.getResults();
    }

    // here's a workhorse
    private void populateResults() {
        if (this.leaves.size() > 1) { // ok, so if postfix provided is more than just 22+, which would just be 2+2 in postfix, do this part
            this.result.add("("); // this seems to be what the example in the hw is doing
            for (int i = this.leaves.size() - 1; i > -1; ) { // step through leaves in backward order
                if (this.leaves.size() == 2 && i - 1 > -1) { // if there are only two leaves, i don't have to worry about complicated parent/child items
                    this.result.add(this.leaves.get(i).toString());
                    i -= 1;
                    this.result.add(this.leaves.get(i).toString());
                    i -= 1;
                } else if (this.leaves.size() > 2 && i - 1 > -1) { // yay, complicated!
                    if ((this.leaves.get(i).getParent() == this.leaves.get(i - 1) && this.leaves.get(i).getParent().getEndpointLeft() != null) || i == 1) {
                        // if the parent leaf is the next leaf, put next leaf in first, then this leaf, as long as parent leaf has a left endpoint
                        this.result.add(this.leaves.get(i - 1).toString());
                        i -= 1;
                        this.result.add(this.leaves.get(i + 1).toString());
                        i -= 1;
                        if (this.result.get(this.result.size()-2).contains("(")){
                            this.result.add(")");
                        } else if (this.result.get(this.result.size()-2).contains(")")){
                            this.result.add("(");
                        }
                    } else {
                        this.result.add(this.leaves.get(i).toString());
                        i -= 1;
                        this.result.add(this.leaves.get(i).toString());
                        i -= 1;
                        if (this.result.get(this.result.size()-2).contains("(") && !this.result.get(this.result.size()-2).contains("(") && this.result.get(this.result.size()-2).matches("\\d+")){
                            this.result.add(")");
                        } else if (this.result.get(this.result.size()-2).contains(")") && !this.result.get(this.result.size()-2).contains("(") && this.result.get(this.result.size()-2).matches("\\d+")){
                            this.result.add("(");
                        }
                    }
                }
                if (i == 0) { // put in the last leaf, and kill the for loop
                    this.result.add(this.leaves.get(i).toString());
                    i -= 1;
                    if (this.result.get(this.result.size()-2).contains("(")){
                        this.result.add(")");
                    } else if (this.result.get(this.result.size()-2).contains(")")){
                        this.result.add("(");
                    }
                }
            }
            this.result.add(")");
            String[] tempArr = this.getResults().replaceAll("\\s+", "").split("");
            this.result.set(0, "");
            // This part is so that the parenthesis match up in the result string
            for (int i = 0; i < tempArr.length; i++){
                String str = tempArr[i];
                if (i-4 >= 0) {
                    if (str.equals(")") && !tempArr[i-4].equals("(")) {
                        String addParens = this.result.get(0) + "(";
                        this.result.set(0, addParens);
                    }
                }
            }
        } else {
            this.result.add(this.leaves.get(0).toString());
        }
    }

    // clean up the result array to present it as a string
    private String getResults() {
        String results = Arrays.deepToString(this.result.toArray());
        results = results.replace("[", "");
        results = results.replace("]", "");
        results = results.replace(",", "");
        results = results.replace(" ", "");
        return results;
    }

    // recursively step through the clean tokenized input
    // this one's a doozy
    private void traverse(String token) {
        if (!this.cleanStack.empty()) { // i need this so that i don't get a stackoverflow
            if (this.arith.contains(token) && this.root == null) { // create a root
                this.root = new Expr(token, null);
                if (!this.arith.contains(this.cleanStack.peek())) { // put in numbers if there are any
                    String right = this.cleanStack.pop();
                    if (!this.arith.contains(this.cleanStack.peek())) {
                        String left = this.cleanStack.pop();
                        this.root.setEndpointLeft(left);
                    }
                    this.root.setEndpointRight(right);
                }
                this.leaves.add(this.root);
                if (!this.cleanStack.empty()) { // can't pop or peek if stack is empty without empty stack error
                    this.traverse(this.cleanStack.pop());
                }
            } else if (this.root != null && !this.cleanStack.empty()) { // will evaluate to true on all subsequent calls
                //this.root.childLeaf(token);
                if (this.arith.contains(token)) {
                    if (!this.arith.contains(this.cleanStack.peek())) {
                        Leaf leaf = new Expr(token, this.leaves.get(this.leaves.size() - 1)); // again, no good reason to use abstract because I'm creating a leaf, but project said to do it
                        String right = this.cleanStack.pop();
                        if (!this.arith.contains(this.cleanStack.peek())) {
                            String left = this.cleanStack.pop();
                            leaf.setEndpointLeft(left);
                        }
                        leaf.setEndpointRight(right);
                        this.leaves.add(leaf);
                        if (!this.cleanStack.empty()) {
                            this.traverse(this.cleanStack.pop());
                        }
                    } else {
                        Leaf leaf = new Expr(token, this.leaves.get(this.leaves.size() - 2));
                        this.leaves.add(leaf);
                        if (!this.cleanStack.empty()) {
                            this.traverse(this.cleanStack.pop());
                        }
                    }
                } else {
                    if (this.leaves.get(this.leaves.size() - 1).getEndpointLeft() == null && !this.cleanStack.empty()) {
                        this.leaves.get(this.leaves.size() - 1).setEndpointLeft(this.cleanStack.pop());
                    } else if (this.leaves.get(this.leaves.size() - 1).getEndpointRight() == null && !this.cleanStack.empty()) {
                        this.leaves.get(this.leaves.size() - 1).setEndpointRight(this.cleanStack.pop());
                    }
                }
                if (!this.cleanStack.empty()) {
                    this.traverse(this.cleanStack.pop());
                }
            }
        }
        if (this.cleanStack.empty()) {
            if (this.leaves.size() >= 2) {
                if (this.leaves.get(this.leaves.size() - 2).getEndpointRight() == null && this.leaves.get(this.leaves.size() - 2).getEndpointLeft() == null) {
                    this.leaves.get(this.leaves.size() - 2).setEndpointLeft(token);
                }
            }
        }
    }


}

abstract class Leaf {

    private String id;
    private Leaf parent;
    private String endpointLeft, endpointRight;

    // empty default constructor because abstract
    Leaf() {

    }

    Leaf(String token, Leaf parent) {
        this.id = token;
        this.endpointLeft = null;
        this.parent = parent;
        this.endpointRight = null;
    }

    private String getId() {
        return this.id;
    }

    Leaf getParent() {
        return this.parent;
    }

    void setEndpointLeft(String endpointLeft) {
        this.endpointLeft = endpointLeft;
    }

    void setEndpointRight(String endpointRight) {
        this.endpointRight = endpointRight;
    }

    String getEndpointLeft() {
        return this.endpointLeft;
    }

    String getEndpointRight() {
        return this.endpointRight;
    }

    public String toString() {
        String postfix;
        if (this.getEndpointLeft() != null && this.getEndpointRight() != null) {
            postfix = "(" + this.getEndpointLeft() + " " + this.getId() + " " + this.getEndpointRight() + ")";
        } else if (this.getEndpointLeft() == null && this.getEndpointRight() != null) {
            postfix = " " + this.getId() + " " + this.getEndpointRight() + ")";
        } else if (this.getEndpointLeft() != null && this.getEndpointRight() == null) {
            postfix = "(" + this.getEndpointLeft() + " " + this.getId() + " ";
        } else {
            postfix = this.getId();
        }
        return postfix;
    }

}

// only here because we need 3 classes.
class Except extends RuntimeException {

    Except(String message) {
        super(message);
    }
}