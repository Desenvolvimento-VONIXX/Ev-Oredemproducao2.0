  package eventoop;

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
import com.sankhya.util.JdbcUtils;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Utilitarios {
   public List<Integer> buscaValorDoParametro() throws MGEModelException {
      String res = "";
      List<Integer> valorDoParamtro = new ArrayList();
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
         sql.appendSql("SELECT\r\n    STUFF((\r\n            SELECT ', ' + VALOR\r\n            FROM AD_PARAMETROS\r\n            WHERE PARAMETRO = :PARAMETRO\r\n            FOR XML PATH('')\r\n        ), 1, 2, ''\r\n    ) AS VALOR;");
         sql.setNamedParameter("PARAMETRO", parametro);

         for(rset = sql.executeQuery(); rset.next(); res = rset.getString("VALOR")) {
         }

         String[] valoresArray = res.trim().split(",");
         String[] var13 = valoresArray;
         int var12 = valoresArray.length;

         for(int var11 = 0; var11 < var12; ++var11) {
            String valor = var13[var11];
            valorDoParamtro.add(Integer.parseInt(valor.trim()));
         }
      } catch (Exception var17) {
         MGEModelException.throwMe(var17);
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
      } catch (Exception var4) {
         var4.printStackTrace();
         MGEModelException.throwMe(var4);
      }

   }
}