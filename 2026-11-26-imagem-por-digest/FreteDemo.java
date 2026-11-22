import java.math.BigDecimal;

public class FreteDemo {
    public static void main(String[] args) {
        var pedido = new Pedido(new BigDecimal("12"));
        System.out.println(new Checkout(new PorPeso(new BigDecimal("2.50"))).resumo(pedido));
        System.out.println(new Checkout(new RetiradaNaLoja()).resumo(pedido));

        // Interface com um único método abstrato aceita lambda
        Frete gratis = p -> BigDecimal.ZERO;
        System.out.println("Lambda: R$ " + gratis.calcular(pedido));
    }
}

// O contrato: só diz O QUE fazer
interface Frete {
    BigDecimal calcular(Pedido p);      // public e abstract sem escrever

    default String nome() {             // default: já vem pronto (Java 8+)
        return getClass().getSimpleName();
    }
}

record Pedido(BigDecimal pesoKg) {}

// As peças que dá para trocar
record PorPeso(BigDecimal porKg) implements Frete {
    public BigDecimal calcular(Pedido p) { return p.pesoKg().multiply(porKg); }
}

record RetiradaNaLoja() implements Frete {
    public BigDecimal calcular(Pedido p) { return BigDecimal.ZERO; }
}

// Quem usa depende do contrato, não da peça
record Checkout(Frete frete) {
    String resumo(Pedido p) { return frete.nome() + ": R$ " + frete.calcular(p); }
}
