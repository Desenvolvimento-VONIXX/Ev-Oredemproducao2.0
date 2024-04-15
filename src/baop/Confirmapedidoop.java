package baop;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.ws.ServiceContext;

public class Confirmapedidoop implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao ctx) throws Exception {
		
		
	    JapeSession.SessionHandle hnd = null;
	    JdbcWrapper jdbc = null;

	    try {
	        Registro linhas[] = ctx.getLinhas();
	        BigDecimal nunota = BigDecimal.ZERO;
	        int contadorLinhas = 0;

	        for (Registro linha : linhas) {
	            nunota = (BigDecimal) linha.getCampo("NUNOTA");
	            contadorLinhas++;

	            
	            for (int i = 0; i < contadorLinhas; i++) {
	                confirma(nunota);
	                
	                ctx.setMensagemRetorno("Pedido confirmado" + nunota);
	            }
	        }
	    } catch (Exception e) {
	        MGEModelException.throwMe(e);
	    } finally {
	        JapeSession.close(hnd);
	        JdbcWrapper.closeSession(jdbc);
	    }
	}

        
    

public String getSessionId() { return ServiceContext.getCurrent().getHttpSessionId(); }//RETORNA SESSION ID DO USUARIO LOGADO
	
	public void confirma(BigDecimal nunota) throws MGEModelException {
		try {						
			AuthenticationInfo authenticationInfo = new AuthenticationInfo("SUP", BigDecimal.ZERO, BigDecimal.ZERO, 0);
			authenticationInfo.makeCurrent();
			AuthenticationInfo.getCurrent();
			BarramentoRegra barramentoConfirmacao = BarramentoRegra.build(CentralFaturamento.class, "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
			barramentoConfirmacao.setValidarSilencioso(true);
			ConfirmacaoNotaHelper.confirmarNota(nunota, barramentoConfirmacao);
		
			
		} catch(Exception e) { e.printStackTrace(); MGEModelException.throwMe(e); }
	}}