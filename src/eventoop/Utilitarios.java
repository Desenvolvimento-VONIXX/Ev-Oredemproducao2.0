package eventoop;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.sankhya.util.JdbcUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class Utilitarios {
    ///PEGANDO TOP DO PEDIDO E COMPARANDO COM AS QUE ESTÃO NO PARAMETRO///////
    public List<Integer> buscaValorDoParametro() throws MGEModelException {

//AD_PARAMETROS
		String res = "";
		List<Integer> valorDoParamtro = new ArrayList<>();
		String parametro = "Top pedido apontamento";
		JdbcWrapper jdbc = null;
		NativeSql sql = null;
		ResultSet rset = null;
		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();
			hnd.setFindersMaxRows(-1);
			EntityFacade entity = EntityFacadeFactory.getDWFFacade();
			jdbc = entity.getJdbcWrapper();
			jdbc.openSession();
			sql = new NativeSql(jdbc);
			sql.appendSql("SELECT\r\n"
					+ "    STUFF((\r\n"
					+ "            SELECT ', ' + VALOR\r\n"
					+ "            FROM AD_PARAMETROS\r\n"
					+ "            WHERE PARAMETRO = :PARAMETRO\r\n"
					+ "            FOR XML PATH('')\r\n"
					+ "        ), 1, 2, ''\r\n"
					+ "    ) AS VALOR;");

			sql.setNamedParameter("PARAMETRO", parametro);
			rset = sql.executeQuery();
			while (rset.next()) {
				res = rset.getString("VALOR");
			} // VALOR É O PARAMETRO

			String[] valoresArray = res.trim().split(",");
			for (String valor : valoresArray) {
				valorDoParamtro.add(Integer.parseInt(valor.trim()));
			}
		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JdbcUtils.closeResultSet(rset);
			NativeSql.releaseResources(sql);
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
		return valorDoParamtro;
	}
    
    public void confirmaPedidoSnk(BigDecimal nuNota) throws MGEModelException {
		try {						
			AuthenticationInfo authenticationInfo = new AuthenticationInfo("SUP", BigDecimal.ZERO, BigDecimal.ZERO, 0);
			authenticationInfo.makeCurrent();
			AuthenticationInfo.getCurrent();
			BarramentoRegra barramentoConfirmacao = BarramentoRegra.build(CentralFaturamento.class, "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
			barramentoConfirmacao.setValidarSilencioso(true);
			ConfirmacaoNotaHelper.confirmarNota(nuNota, barramentoConfirmacao);
					
			
		} catch(Exception e) { e.printStackTrace(); MGEModelException.throwMe(e); }
	}
}

