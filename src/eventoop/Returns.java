   package eventoop;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class Returns implements EventoProgramavelJava {
   public void afterDelete(PersistenceEvent arg0) throws Exception {
   }

   public void afterInsert(PersistenceEvent arg0) throws Exception {
   }

   public void afterUpdate(PersistenceEvent event) throws Exception {
   }

   public void beforeCommit(TransactionContext arg0) throws Exception {
   }

   public void beforeDelete(PersistenceEvent arg0) throws Exception {
   }

   public void beforeInsert(PersistenceEvent arg0) throws Exception {
   }

   public void beforeUpdate(PersistenceEvent event) throws Exception {
      DynamicVO OldVo = (DynamicVO)event.getOldVO();
      if (OldVo.asBigDecimal("AD_REMESSAORIGEM") != null) {
         BigDecimal previous = OldVo.asBigDecimal("QTDNEG");
         DynamicVO ite = (DynamicVO)event.getVo();
         BigDecimal later = ite.asBigDecimal("QTDNEG");
         BigDecimal nunota = ite.asBigDecimal("AD_REMESSAORIGEM");
         BigDecimal codprod = ite.asBigDecimal("CODPROD");
         if (!later.equals(previous)) {
            this.atualiza(later, nunota, codprod, previous);
         }
      }

   }

   private void atualiza(BigDecimal later, BigDecimal nunota, BigDecimal codprod, BigDecimal previous) throws Exception {
      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
      JdbcWrapper jdbc = entity.getJdbcWrapper();
      SessionHandle hnd = JapeSession.open();
      BigDecimal novaquantidade = previous.subtract(later);
      ImpostosHelpper imp = new ImpostosHelpper();

      try {
         String sql1 = "SELECT QTDNEG FROM TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD =:CODPROD";
         NativeSql query1 = new NativeSql(jdbc);
         query1.setNamedParameter("CODPROD", codprod);
         query1.setNamedParameter("NUNOTA", nunota);
         query1.appendSql(sql1);
         ResultSet rset1 = query1.executeQuery();
         if (rset1.next()) {
            BigDecimal qtdbefore = rset1.getBigDecimal("QTDNEG");
            BigDecimal qtdfinal = qtdbefore.add(novaquantidade);
            NativeSql sql = new NativeSql(jdbc);
            sql.appendSql("UPDATE TGFITE SET QTDNEG = :QTDFINAL WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD");
            sql.setNamedParameter("CODPROD", codprod);
            sql.setNamedParameter("NUNOTA", nunota);
            sql.setNamedParameter("QTDFINAL", qtdfinal);
            sql.executeUpdate();
            imp.calcularTotalItens(nunota, false);
            imp.totalizarNota(nunota);
         }
      } catch (Exception var19) {
         MGEModelException.throwMe(var19);
      } finally {
         JapeSession.close(hnd);
         JdbcWrapper.closeSession(jdbc);
      }

   }
}