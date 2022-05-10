import br.com.alura.leilao.dao.LeilaoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Usuario;
import br.com.alura.leilao.service.EnviadorDeEmails;
import br.com.alura.leilao.service.FinalizarLeilaoService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class FinalizarLeilaoServiceTest {

    private FinalizarLeilaoService service;

    @Mock
    private LeilaoDao dao;

    @Mock
    private EnviadorDeEmails enviadorDeEmails;

    @BeforeEach
    public void beforeEach(){
        MockitoAnnotations.initMocks(this);
        this.service = new FinalizarLeilaoService(dao,enviadorDeEmails);
    }

    @Test
    void deveriaFinalizarUmLeilao(){
        List<Leilao> leiloes = leiloes();


        Mockito.when(dao.buscarLeiloesExpirados())
                        .thenReturn(leiloes);


        service.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get(0);
        Assert.assertTrue(leilao.isFechado());
        Assert.assertEquals(new BigDecimal("900"), leilao.getLanceVencedor().getValor());

        Mockito.verify(dao).salvar(leilao);
    }

    @Test
    void deveriaEnviarEmailParaMaiorLance(){
        List<Leilao> leiloes = leiloes();


        Mockito.when(dao.buscarLeiloesExpirados())
                        .thenReturn(leiloes);
        service.finalizarLeiloesExpirados();
        Leilao leilao = leiloes.get(0);
        Mockito.verify(enviadorDeEmails).enviarEmailVencedorLeilao(leilao.getLanceVencedor());
    }

    @Test
    void napDeveEnviarEmailEmCasoDeErrorEncerrarOLeilao(){
        List<Leilao> leiloes = leiloes();


        Mockito.when(dao.buscarLeiloesExpirados())
                .thenReturn(leiloes);

        Mockito.when(dao.salvar(Mockito.any()))
                        .thenThrow(RuntimeException.class);

        try {
            service.finalizarLeiloesExpirados();
            Mockito.verifyNoInteractions(enviadorDeEmails);
        } catch (Exception e) {}

    }

    private List<Leilao> leiloes(){
        List<Leilao> lista = new ArrayList<>();

        Leilao leilao = new Leilao("Celular",new BigDecimal("500"),new Usuario("Fulano"));
        Lance lance = new Lance(new Usuario("Beltrano"),new BigDecimal("600"));
        Lance lance2 = new Lance(new Usuario("Ciclano"),new BigDecimal("900"));

        leilao.propoe(lance);
        leilao.propoe(lance2);

        lista.add(leilao);

        return lista;
    }
}
