package io.escriba;

public class T3<A, B, C> extends T2<A, B> {
	public final C c;

	public T3(A a, B b, C c) {
		super(a, b);
		this.c = c;
	}

	public static <A, B, C> T3<A, B, C> of(A a, B b, C c) {
		return new T3(a, b, c);
	}
}
