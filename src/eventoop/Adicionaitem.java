  package eventoop;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Adicionaitem {
   public void consultaPreco(BigDecimal nunota, BigDecimal codprod, BigDecimal quantidade, BigDecimal nunotaorigem, BigDecimal nunpedevox) throws MGEModelException {
      JdbcWrapper jdbc = null;
      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
      jdbc = entity.getJdbcWrapper();

      try {
         jdbc.openSession();
         NativeSql query = new NativeSql(jdbc);
         query.setNamedParameter("CODPROD", codprod);
         query.appendSql("SELECT VLRVENDA FROM TGFEXC, TGFTAB WHERE\r\nTGFTAB.NUTAB = TGFEXC.NUTAB AND TGFTAB.CODTAB = 6\r\nAND  DTVIGOR =(SELECT MAX( DTVIGOR) FROM TGFTAB TAB1 WHERE TGFTAB.CODTAB = TAB1.CODTAB)AND CODPROD = :CODPROD");
         ResultSet rset = query.executeQuery();
         if (rset.next()) {
            BigDecimal vlr = rset.getBigDecimal("VLRVENDA");
            if (nunpedevox == null) {
               this.adicionaitens(nunota, codprod, quantidade, vlr, nunotaorigem);
            } else {
               this.adicionaitensevox(nunota, codprod, quantidade, vlr, nunotaorigem, nunpedevox);
            }
         }
      } catch (Exception var14) {
         var14.printStackTrace();
         MGEModelException.throwMe(var14);
         System.out.println("Erro ao Executar Evento consultaPreco" + var14.getCause() + var14.getMessage());
      } finally {
         JdbcWrapper.closeSession(jdbc);
      }

   }

   public void adicionaitens(BigDecimal nunota, BigDecimal codprod, BigDecimal quantidade, BigDecimal vlr, BigDecimal nunotaorigem) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         JapeWrapper proDAO = JapeFactory.dao("Produto");
         JapeWrapper iteDAO = JapeFactory.dao("ItemNota");
         DynamicVO pro = ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)iteDAO.create().set("CODEMP", BigDecimal.valueOf(1L))).set("CODPROD", codprod)).set("NUNOTA", BigDecimal.valueOf(24L))).set("QTDNEG", quantidade)).set("CODVOL", proDAO.findByPK(new Object[]{codprod}).asString("CODVOL"))).set("VLRUNIT", vlr)).set("VLRTOT", vlr.multiply(quantidade))).set("CODLOCALORIG", BigDecimal.valueOf(1030000L))).set("RESERVA", "S")).set("ATUALESTOQUE", BigDecimal.valueOf(1L))).set("CODVEND", BigDecimal.valueOf(0L))).set("VLRDESC", BigDecimal.valueOf(0.0D))).set("PERCDESC", BigDecimal.valueOf(0.0D))).set("BASEICMS", BigDecimal.valueOf(0.0D))).set("VLRICMS", BigDecimal.valueOf(0.0D))).set("ALIQICMS", BigDecimal.valueOf(0.0D))).set("BASEIPI", BigDecimal.valueOf(0.0D))).set("VLRIPI", BigDecimal.valueOf(0.0D))).set("ALIQIPI", BigDecimal.valueOf(0.0D))).set("AD_APONTAMENTO", BigDecimal.valueOf(0.0D))).set("PENDENTE", "S")).save();
         BigDecimal vlrtotCab = this.selecionarItens(BigDecimal.valueOf(24L));
         this.atualizarValorCAB(vlrtotCab, BigDecimal.valueOf(24L));
         this.updatenunota(nunotaorigem);
      } catch (Exception var14) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         var14.printStackTrace(pw);
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void adicionaitensevox(BigDecimal nunota, BigDecimal codprod, BigDecimal quantidade, BigDecimal vlr, BigDecimal nunotaorigem, BigDecimal nunpedevox) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         JapeWrapper proDAO = JapeFactory.dao("Produto");
         JapeWrapper iteDAO = JapeFactory.dao("ItemNota");
         DynamicVO pro = ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)iteDAO.create().set("CODEMP", BigDecimal.valueOf(1L))).set("CODPROD", codprod)).set("NUNOTA", BigDecimal.valueOf(24L))).set("QTDNEG", quantidade)).set("CODVOL", proDAO.findByPK(new Object[]{codprod}).asString("CODVOL"))).set("VLRUNIT", vlr)).set("VLRTOT", vlr.multiply(quantidade))).set("CODLOCALORIG", BigDecimal.valueOf(1030000L))).set("RESERVA", "S")).set("ATUALESTOQUE", BigDecimal.valueOf(1L))).set("CODVEND", BigDecimal.valueOf(0L))).set("VLRDESC", BigDecimal.valueOf(0.0D))).set("PERCDESC", BigDecimal.valueOf(0.0D))).set("BASEICMS", BigDecimal.valueOf(0.0D))).set("VLRICMS", BigDecimal.valueOf(0.0D))).set("ALIQICMS", BigDecimal.valueOf(0.0D))).set("BASEIPI", BigDecimal.valueOf(0.0D))).set("VLRIPI", BigDecimal.valueOf(0.0D))).set("ALIQIPI", BigDecimal.valueOf(0.0D))).set("AD_APONTAMENTO", BigDecimal.valueOf(0.0D))).set("AD_NUNPEDEVOX", nunpedevox)).set("PENDENTE", "S")).save();
         BigDecimal vlrtotCab = this.selecionarItens(BigDecimal.valueOf(24L));
         this.atualizarValorCAB(vlrtotCab, BigDecimal.valueOf(24L));
         this.updatenunota(nunotaorigem);
      } catch (Exception var15) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         var15.printStackTrace(pw);
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void atualizarValorCAB(BigDecimal vlrtotCab, BigDecimal nunota) throws SQLException {
      SessionHandle hnd = JapeSession.open();
      hnd.setFindersMaxRows(-1);
      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
      JdbcWrapper jdbc = entity.getJdbcWrapper();
      jdbc.openSession();

      try {
         NativeSql sql = new NativeSql(jdbc);
         sql.appendSql("UPDATE TGFCAB SET VLRNOTA =:VLRNOTA WHERE NUNOTA = :NUNOTA");
         sql.setNamedParameter("NUNOTA", nunota);
         sql.setNamedParameter("VLRNOTA", vlrtotCab);
         sql.executeUpdate();
      } catch (Exception var10) {
         var10.printStackTrace();
      } finally {
         JdbcWrapper.closeSession(jdbc);
         JapeSession.close(hnd);
      }

   }

   public BigDecimal selecionarItens(BigDecimal nunota) throws MGEModelException {
      JdbcWrapper jdbc = null;
      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
      jdbc = entity.getJdbcWrapper();
      BigDecimal vlrTotalCAB = BigDecimal.ZERO;

      try {
         jdbc.openSession();
         NativeSql query = new NativeSql(jdbc);
         query.setNamedParameter("NUNOTA", nunota);
         query.appendSql("SELECT SUM(VLRTOT) AS VLRTOTS FROM TGFITE WHERE NUNOTA=:NUNOTA");
         ResultSet rset = query.executeQuery();
         if (rset.next()) {
            vlrTotalCAB = rset.getBigDecimal("VLRTOTS");
         }
      } catch (Exception var10) {
         var10.printStackTrace();
         MGEModelException.throwMe(var10);
         System.out.println("Erro ao Executar Evento consultaPreco" + var10.getCause() + var10.getMessage());
      } finally {
         JdbcWrapper.closeSession(jdbc);
      }

      return vlrTotalCAB;
   }

   private void updatenunota(BigDecimal nunotaorigem) throws Exception {
      ImpostosHelpper imp = new ImpostosHelpper();
      SessionHandle hnd = JapeSession.open();
      hnd.setFindersMaxRows(-1);
      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
      JdbcWrapper jdbc = entity.getJdbcWrapper();
      jdbc.openSession();

      try {
         NativeSql sql = new NativeSql(jdbc);
         sql.appendSql("UPDATE TGFCAB SET AD_OPLANCADA = :LANCADA WHERE NUNOTA = :NUNOTA");
         sql.setNamedParameter("NUNOTA", nunotaorigem);
         sql.setNamedParameter("LANCADA", "SIM");
         sql.executeUpdate();
         imp.calcularTotalItens(BigDecimal.valueOf(24L), true);
         imp.totalizarNota(BigDecimal.valueOf(24L));
      } catch (Exception var10) {
         var10.printStackTrace();
      } finally {
         JdbcWrapper.closeSession(jdbc);
         JapeSession.close(hnd);
      }

   }

   public void updateintens(BigDecimal codprodorigem, BigDecimal quantidadeorigem, BigDecimal quantidade, BigDecimal nunota) throws SQLException {
      SessionHandle hnd = JapeSession.open();
      hnd.setFindersMaxRows(-1);
      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
      JdbcWrapper jdbc = entity.getJdbcWrapper();
      jdbc.openSession();
      BigDecimal total = quantidadeorigem.add(quantidade);

      try {
         NativeSql sql = new NativeSql(jdbc);
         sql.appendSql("UPDATE TGFITE SET QTDNEG = :QTDNEG WHERE NUNOTA = :NUNOTA AND CODPROD=:CODPROD");
         sql.setNamedParameter("NUNOTA", nunota);
         sql.setNamedParameter("QTDNEG", total);
         sql.setNamedParameter("CODPROD", codprodorigem);
         sql.executeUpdate();
      } catch (Exception var13) {
         var13.printStackTrace();
      } finally {
         JdbcWrapper.closeSession(jdbc);
         JapeSession.close(hnd);
      }

   }
}