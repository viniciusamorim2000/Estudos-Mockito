import br.com.alura.leilao.dao.LeilaoDao;
import br.com.alura.leilao.dao.PagamentoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Pagamento;
import br.com.alura.leilao.model.Usuario;
import br.com.alura.leilao.service.EnviadorDeEmails;
import br.com.alura.leilao.service.FinalizarLeilaoService;
import br.com.alura.leilao.service.GeradorDePagamento;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class GeradorPagamentoTest {

    private GeradorDePagamento geradorDePagamento;

    @Mock
    private PagamentoDao pagamentoDao;

    @Captor
    private ArgumentCaptor<Pagamento> captor;

    @Mock
    private Clock clock;

    @BeforeEach
    public void beforeEach(){
        MockitoAnnotations.initMocks(this);
        this.geradorDePagamento = new GeradorDePagamento(pagamentoDao,clock);
    }

    @Test
    void devereiaCriarPagamentoParaVencedorDoLeilao(){
        Leilao leilao = leiloes();
        Lance vencedor = leilao.getLanceVencedor();

        LocalDate data = LocalDate.of(2022, 5, 10);

        Instant instant = data.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Mockito.when(clock.instant()).thenReturn(instant);
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        geradorDePagamento.gerarPagamento(vencedor);

        Mockito.verify(pagamentoDao).salvar(captor.capture());
        Pagamento pagamento = captor.getValue();

        Assert.assertEquals(LocalDate.now().plusDays(1),
                pagamento.getVencimento());
        Assert.assertEquals(vencedor.getValor(), pagamento.getValor());
        Assert.assertFalse(pagamento.getPago());
        Assert.assertEquals(vencedor.getUsuario(),pagamento.getUsuario());
        Assert.assertEquals(leilao, pagamento.getLeilao());

    }


    private Leilao leiloes(){
        List<Leilao> lista = new ArrayList<>();

        Leilao leilao = new Leilao("Celular",new BigDecimal("500"),new Usuario("Fulano"));

        Lance lance2 = new Lance(new Usuario("Ciclano"),new BigDecimal("900"));


        leilao.propoe(lance2);
        leilao.setLanceVencedor(lance2);

        return leilao;
    }
}
