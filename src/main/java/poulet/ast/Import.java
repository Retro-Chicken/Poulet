package poulet.ast;

public class Import extends TopLevel {
    public String fileName;

    public Import(String fileName) {
        this.fileName = fileName;
    }
}
