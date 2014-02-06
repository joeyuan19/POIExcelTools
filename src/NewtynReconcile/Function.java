package NewtynReconcile;

public interface Function {
	public String getFunctionName();
	public void launch(Task task);
	public void close();
}
