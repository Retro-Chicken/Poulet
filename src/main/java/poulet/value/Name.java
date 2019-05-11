package poulet.value;

public interface Name {
    Name offset(int offset);
    Name increment();
    Name decrement();
    boolean isFree();
    Integer getIndex();
    String getName();
}
