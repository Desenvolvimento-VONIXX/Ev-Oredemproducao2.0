 package eventoop;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.core.JapeSession.TXBlock;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.mgecomercial.model.facades.helpper.FaturamentoHelper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.CentralFaturamento.ConfiguracaoFaturamento;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Native;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Utilitario_HTML {

   
   public void lancarItensNaNota(BigDecimal nunota) throws MGEModelException {
		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;
		NativeSql query = null;
		ResultSet rset = null;
		
		try {
	         hnd = JapeSession.open();
	         hnd.setFindersMaxRows(-1);
	         EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	         jdbc = entity.getJdbcWrapper();
	         jdbc.openSession();
	         query = new NativeSql(jdbc);
	         query.appendSql("SELECT DISTINCT CODPROD,\r\n"
	         		+ "(SELECT SUM(QTDNEG) FROM SANKHYA.AD_LOGAPONTAMENTO WHERE NUNOTA = :NUNOTA AND CODPROD = APON.CODPROD) AS QTDNEG,\r\n"
	         		+ "(SELECT TOP 1 VLRUNIT FROM SANKHYA.AD_LOGAPONTAMENTO WHERE NUNOTA = :NUNOTA AND CODPROD = APON.CODPROD) AS VLRUNIT,\r\n"
	         		+ "\r\n"
	         		+ "(SELECT TOP 1 CODVOL FROM SANKHYA.AD_LOGAPONTAMENTO WHERE NUNOTA = :NUNOTA AND CODPROD = APON.CODPROD) AS CODVOL,\r\n"
	         		+ "(SELECT TOP 1 VOLUME FROM SANKHYA.AD_LOGAPONTAMENTO WHERE NUNOTA = :NUNOTA AND CODPROD = APON.CODPROD) AS VOLUME\r\n"
	         		+ "FROM SANKHYA.AD_LOGAPONTAMENTO APON WHERE NUNOTA = :NUNOTA");
	         query.setNamedParameter("NUNOTA", nunota);
	         rset = query.executeQuery();
	         if (rset.next()) {
	             
	             BigDecimal codprod = rset.getBigDecimal("CODPROD");
	             BigDecimal qtdneg = rset.getBigDecimal("QTDNEG");
	             BigDecimal vlrunit = rset.getBigDecimal("VLRUNIT");
	             String codvol = rset.getString("CODVOL");
	             BigDecimal volume = rset.getBigDecimal("VOLUME");
	             
	             //LANCAR PRODUTOS
	             
	             
	             if(volume.intValue() == 6) {
	            	lancarBombona(nunota, qtdneg, BigDecimal.valueOf(24));
	            	
	             }
	             
	             sbtraiApontamento(nunota,codprod,vlrunit,volume);
	            
	         }
		}catch (Exception e) {
			e.printStackTrace();
			MGEModelException.throwMe(e);
		}
		
   }

   	//
   private void sbtraiApontamento(BigDecimal nunota, BigDecimal codprod, BigDecimal vlrunit,BigDecimal volume) throws MGEModelException {
	SessionHandle hnd = null;
	JdbcWrapper jdbc = null;
	NativeSql query = null;
	ResultSet rset = null;
	
	//SOMA A QUANTIDADE LANÇADA PELA SEQUENCIA E SUBTRAI DA SEQUENCIA CORRETA NO PEDIDO 24 
	//
	try {
		hnd = JapeSession.open();
        hnd.setFindersMaxRows(-1);
        EntityFacade entity = EntityFacadeFactory.getDWFFacade();
        jdbc = entity.getJdbcWrapper();
        jdbc.openSession();
        query = new NativeSql(jdbc);
        query.appendSql("WITH CTE AS (\r\n"
        		+ "SELECT APON.CODPROD, APON.SEQUENCIA, APON.QTDNEG  FROM SANKHYA.AD_LOGAPONTAMENTO APON\r\n"
        		+ "WHERE APON.NUNOTA = :NUNOTA AND APON.CODPROD = :CODPROD\r\n"
        		+ ") \r\n"
        		+ "SELECT CTE.CODPROD, CTE.SEQUENCIA,(ITE.QTDNEG - SUM(CTE.QTDNEG)) AS QTDNEG_NOVO \r\n"
        		+ "FROM CTE \r\n"
        		+ "JOIN SANKHYA.TGFITE  ITE ON ITE.CODPROD = CTE.CODPROD AND ITE.SEQUENCIA = CTE.SEQUENCIA\r\n"
        		+ "GROUP BY CTE.CODPROD,CTE.SEQUENCIA,ITE.QTDNEG");
        query.setNamedParameter("NUNOTA", nunota);
        query.setNamedParameter("CODPROD", codprod);
        rset = query.executeQuery();
        if(rset.next()) {
        	BigDecimal qtdneg = rset.getBigDecimal("QTDNEG_NOVO");
        	BigDecimal sequencia = rset.getBigDecimal("SEQUENCIA");
        	BigDecimal vlrTotal = qtdneg.multiply(vlrunit);
        	
        	NativeSql sql = new NativeSql(jdbc);
        	sql.appendSql("UPDATE TGFITE SET QTDNEG = :QTDNEG, VLRTOT = :VLRTOT WHERE NUNOTA = 24 AND SEQUENCIA = :SEQUENCIA");
        	sql.setNamedParameter("VLRTOT", vlrTotal);
        	sql.setNamedParameter("QTDNEG", qtdneg);
        	sql.setNamedParameter("SEQUENCIA", sequencia);
        	
        	if(volume.intValue() == 6) {
        		subtraiBombona(qtdneg,sequencia.add(BigDecimal.valueOf(1)));
        	}
        	// qtdneg e sequencia  - log apontamento 
        	// pegar a qtdneg da nota 24
        	// subtrair o valor 
        	// fazer o update
        	
        	
        }
	}catch (Exception e) {
		e.printStackTrace();
		MGEModelException.throwMe(e);
	}
}



   private void subtraiBombona(BigDecimal qtdneg, BigDecimal sequencia) {
		
		
	}

public boolean verificaNotaEVOX(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal sequencia, BigDecimal apontamento, BigDecimal remessaOrigem, BigDecimal volume, BigDecimal nunpedevox) throws MGEModelException {
      boolean resultado = false;
      SessionHandle hnd = null;
      JdbcWrapper jdbc = null;
      NativeSql query = null;
      ResultSet rset = null;
      System.out.println("função verificaNota");

      try {
         hnd = JapeSession.open();
         hnd.setFindersMaxRows(-1);
         EntityFacade entity = EntityFacadeFactory.getDWFFacade();
         jdbc = entity.getJdbcWrapper();
         jdbc.openSession();
         query = new NativeSql(jdbc);
         query.setNamedParameter("CODPROD", codprod);
         query.setNamedParameter("NUNOTA", nunota);
         query.setNamedParameter("AD_NUNPEDEVOX", nunpedevox);
         query.appendSql("SELECT QTDNEG FROM SANKHYA.TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD AND AD_NUNPEDEVOX=:AD_NUNPEDEVOX");
         rset = query.executeQuery();
         if (rset.next()) {
            resultado = true;
            BigDecimal qtdAnterior = rset.getBigDecimal("QTDNEG");
            this.somaProdEVOX(nunota, codprod, qtdneg, qtdAnterior, volume, remessaOrigem, nunpedevox);
            this.subtrairDoApontamento(codprod, sequencia, apontamento, qtdneg, remessaOrigem);
         }
      } catch (Exception var19) {
         var19.printStackTrace();
         MGEModelException.throwMe(var19);
         System.out.println("Erro ao Executar consultaPreco" + var19.getCause() + var19.getMessage());
      } finally {
         JdbcWrapper.closeSession(jdbc);
         JapeSession.close(hnd);
      }

      return resultado;
   }





   public void somaProdEVOX(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal qtdAnterior, BigDecimal volume, BigDecimal remessaOrigem, BigDecimal nunpedevox) throws Exception {
      BigDecimal novavariavel = qtdneg.add(qtdAnterior);
      ImpostosHelpper imp = new ImpostosHelpper();
      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
      JdbcWrapper jdbc = entity.getJdbcWrapper();
      jdbc.openSession();
      NativeSql query = new NativeSql(jdbc);
      query.setNamedParameter("NUNOTA", nunota);
      query.setNamedParameter("CODPROD", codprod);
      query.setNamedParameter("AD_NUNPEDEVOX", nunpedevox);
      query.appendSql("SELECT VLRUNIT FROM TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD AND AD_NUNPEDEVOX=:AD_NUNPEDEVOX");
      ResultSet rset = query.executeQuery();
      if (rset.next()) {
         BigDecimal vlrunitario = rset.getBigDecimal("VLRUNIT");

         try {
            NativeSql sql = new NativeSql(jdbc);
            sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD AND AD_NUNPEDEVOX=:AD_NUNPEDEVOX");
            sql.setNamedParameter("NUNOTA", nunota);
            sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novavariavel));
            sql.setNamedParameter("QUANTIDADE", novavariavel);
            sql.setNamedParameter("CODPROD", codprod);
            sql.setNamedParameter("AD_NUNPEDEVOX", nunpedevox);
            sql.executeUpdate();
            imp.calcularImpostos(nunota);
            imp.setForcarRecalculo(true);
            imp.totalizarNota(nunota);
         } catch (Exception var19) {
            var19.printStackTrace();
         } finally {
            JdbcWrapper.closeSession(jdbc);
         }
      }

   }

 

   public void atualizaNotaOrigemEVOX(BigDecimal nunotaOrigem, BigDecimal qtdneg, BigDecimal qtdAnterior, BigDecimal sequencia, BigDecimal codprod, BigDecimal vlrunitario, BigDecimal nunpedevox) throws Exception {
      BigDecimal novoQTD = qtdneg.add(qtdAnterior);
      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
      JdbcWrapper jdbc = entity.getJdbcWrapper();
      jdbc.openSession();

      try {
         NativeSql sql = new NativeSql(jdbc);
         sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA  AND SEQUENCIA = :SEQUENCIA AND AD_NUNPEDEVOX=:AD_NUNPEDEVOX");
         sql.setNamedParameter("AD_NUNPEDEVOX", nunpedevox);
         sql.setNamedParameter("SEQUENCIA", sequencia);
         sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novoQTD));
         sql.setNamedParameter("QUANTIDADE", novoQTD);
         sql.setNamedParameter("CODPROD", codprod);
         sql.setNamedParameter("NUNOTA", nunotaOrigem);
         sql.executeUpdate();
      } catch (Exception var15) {
         var15.printStackTrace();
      } finally {
         JdbcWrapper.closeSession(jdbc);
      }

   }



   public void adicionaBombona(BigDecimal nunota, BigDecimal qtdneg, BigDecimal remessaOrigem, BigDecimal sequencia) throws MGEModelException {
      boolean existeBombona = false;
      SessionHandle hnd = null;
      JdbcWrapper jdbc = null;
      NativeSql query = null;
      ResultSet rset = null;
      System.out.println("função verificaNota");

      try {
         hnd = JapeSession.open();
         EntityFacade entity = EntityFacadeFactory.getDWFFacade();
         jdbc = entity.getJdbcWrapper();
         jdbc.openSession();
         query = new NativeSql(jdbc);
         query.setNamedParameter("NUNOTA", nunota);
         query.appendSql("SELECT QTDNEG,VLRUNIT FROM SANKHYA.TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = 4008001");
         rset = query.executeQuery();
         if (rset.next()) {
            existeBombona = true;
            BigDecimal qtdnegAnterior = rset.getBigDecimal("QTDNEG");
            BigDecimal novavariavel = qtdnegAnterior.add(qtdneg);
            BigDecimal vlrunitario = rset.getBigDecimal("VLRUNIT");
            NativeSql sql = new NativeSql(jdbc);
            sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT  WHERE NUNOTA = :NUNOTA AND CODPROD = 4008001");
            sql.setNamedParameter("NUNOTA", nunota);
            sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novavariavel));
            sql.setNamedParameter("QUANTIDADE", novavariavel);
            sql.executeUpdate();
         }

         if (!existeBombona) {
            this.lancarBombona(nunota, qtdneg, remessaOrigem);
         }

         this.subtraiBombonaApontamento(remessaOrigem, qtdneg, sequencia);
      } catch (Exception var18) {
         var18.printStackTrace();
         MGEModelException.throwMe(var18);
         System.out.println("Erro ao Executar consultaPreco" + var18.getCause() + var18.getMessage());
      } finally {
         JdbcWrapper.closeSession(jdbc);
         JapeSession.close(hnd);
      }

   }

   private void subtraiBombonaApontamento(BigDecimal remessaOrigem, BigDecimal qtdneg, BigDecimal sequencia2) throws MGEModelException {
      SessionHandle hnd = null;
      JdbcWrapper jdbc = null;
      NativeSql query = null;
      ResultSet rset = null;

      try {
         hnd = JapeSession.open();
         EntityFacade entity = EntityFacadeFactory.getDWFFacade();
         jdbc = entity.getJdbcWrapper();
         jdbc.openSession();
         query = new NativeSql(jdbc);
         query.setNamedParameter("SEQUENCIA", sequencia2);
         query.setNamedParameter("NUNOTA", remessaOrigem);
         query.appendSql("SELECT TOP 1 QTDNEG,VLRUNIT,SEQUENCIA FROM SANKHYA.TGFITE\r\nWHERE NUNOTA = :NUNOTA AND CODPROD = 4008001 AND QTDNEG = (SELECT QTDNEG FROM SANKHYA.TGFITE WHERE NUNOTA =:NUNOTA AND SEQUENCIA = :SEQUENCIA)");
         rset = query.executeQuery();
         if (rset.next()) {
            BigDecimal qtdnegAnterior = rset.getBigDecimal("QTDNEG");
            BigDecimal sequencia = rset.getBigDecimal("SEQUENCIA");
            BigDecimal novavariavel = qtdnegAnterior.subtract(qtdneg);
            BigDecimal vlrunitario = rset.getBigDecimal("VLRUNIT");
            NativeSql sql = new NativeSql(jdbc);
            sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT  WHERE NUNOTA = :NUNOTA AND CODPROD = 4008001 AND SEQUENCIA=:SEQUENCIA");
            sql.setNamedParameter("NUNOTA", remessaOrigem);
            sql.setNamedParameter("SEQUENCIA", sequencia);
            sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novavariavel));
            sql.setNamedParameter("QUANTIDADE", novavariavel);
            sql.executeUpdate();
         }
      } catch (Exception var17) {
         var17.printStackTrace();
         MGEModelException.throwMe(var17);
         System.out.println("Erro ao Executar consultaPreco" + var17.getCause() + var17.getMessage());
      } finally {
         JdbcWrapper.closeSession(jdbc);
         JapeSession.close(hnd);
      }

   }

   public void lancarBombona(BigDecimal nunota, BigDecimal qtdneg, BigDecimal remessaOrigem) throws Exception {
      BigDecimal codprod = BigDecimal.valueOf(4008001L);
      String where = "CODPROD = 4008001 AND NUNOTA= " + remessaOrigem;
      BigDecimal vlrunit = NativeSql.getBigDecimal("VLRUNIT", "TGFITE", where);
      BigDecimal vlrtotal = qtdneg.multiply(vlrunit);
      ImpostosHelpper imp = new ImpostosHelpper();
      

      try {
         JapeWrapper proDAO = JapeFactory.dao("Produto");
         JapeWrapper iteDAO = JapeFactory.dao("ItemNota");
         ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)iteDAO.create().set("CODEMP", BigDecimal.valueOf(1L))).set("CODPROD", codprod)).set("NUNOTA", nunota)).set("QTDNEG", qtdneg)).set("CODVOL", proDAO.findByPK(new Object[]{codprod}).asString("CODVOL"))).set("VLRUNIT", vlrunit)).set("VLRTOT", vlrtotal)).set("CODLOCALORIG", BigDecimal.valueOf(1030000L))).set("RESERVA", "N")).set("ATUALESTOQUE", BigDecimal.valueOf(-1L))).set("CODVEND", BigDecimal.valueOf(0L))).set("VLRDESC", BigDecimal.valueOf(0.0D))).set("CODCFO", BigDecimal.valueOf(5905L))).set("CODTRIB", BigDecimal.valueOf(41L))).set("PERCDESC", BigDecimal.valueOf(0.0D))).set("BASEICMS", BigDecimal.valueOf(0.0D))).set("VLRICMS", BigDecimal.valueOf(0.0D))).set("ALIQICMS", BigDecimal.valueOf(0.0D))).set("BASEIPI", BigDecimal.valueOf(0.0D))).set("VLRIPI", BigDecimal.valueOf(0.0D))).set("ALIQIPI", BigDecimal.valueOf(0.0D))).set("CODLOCALTERC", BigDecimal.valueOf(1030000L))).set("ATUALESTTERC", "P")).set("TERCEIROS", "S")).set("PENDENTE", "S")).set("AD_CUSTOMIZACAO", BigDecimal.valueOf(24L))).set("AD_REMESSAORIGEM", remessaOrigem)).save();
         System.out.println("###################função additensLog");
         imp.calcularImpostos(nunota);
         imp.setForcarRecalculo(true);
         imp.totalizarNota(nunota);
      } catch (Exception var13) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         var13.printStackTrace(pw);
      }

   }
   
   //ADICIONAR ITEM NA NOTA
   public void additensLog(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, String codvol, BigDecimal vlrunit, BigDecimal vlrtotal, BigDecimal remessaOrigem, BigDecimal sequencia, BigDecimal apontamento, BigDecimal volume, BigDecimal nunpedevox) throws Exception {
	      ImpostosHelpper imp = new ImpostosHelpper();
	      Object var13 = null;

	      try {
	         JapeWrapper proDAO = JapeFactory.dao("Produto");
	         JapeWrapper iteDAO = JapeFactory.dao("ItemNota");
	         ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)iteDAO.create()
	        		 .set("CODEMP", BigDecimal.valueOf(1L))).set("CODPROD", codprod)).set("NUNOTA", nunota)).set("QTDNEG", qtdneg)).set("CODVOL", proDAO.findByPK(new Object[]{codprod}).asString("CODVOL"))).set("VLRUNIT", vlrunit)).set("VLRTOT", vlrtotal)).set("CODLOCALORIG", BigDecimal.valueOf(1030000L))).set("RESERVA", "N")).set("ATUALESTOQUE", BigDecimal.valueOf(-1L))).set("CODVEND", BigDecimal.valueOf(0L))).set("VLRDESC", BigDecimal.valueOf(0.0D))).set("CODCFO", BigDecimal.valueOf(5905L))).set("CODTRIB", BigDecimal.valueOf(41L))).set("PERCDESC", BigDecimal.valueOf(0.0D))).set("BASEICMS", BigDecimal.valueOf(0.0D))).set("VLRICMS", BigDecimal.valueOf(0.0D))).set("ALIQICMS", BigDecimal.valueOf(0.0D))).set("BASEIPI", BigDecimal.valueOf(0.0D))).set("VLRIPI", BigDecimal.valueOf(0.0D))).set("ALIQIPI", BigDecimal.valueOf(0.0D))).set("CODLOCALTERC", BigDecimal.valueOf(1030000L))).set("ATUALESTTERC", "P")).set("TERCEIROS", "S")).set("PENDENTE", "S")).set("AD_NUNPEDEVOX", nunpedevox)).set("AD_REMESSAORIGEM", remessaOrigem)).set("AD_CUSTOMIZACAO", BigDecimal.valueOf(24L))).save();
	         this.subtrairDoApontamento(codprod, sequencia, apontamento, qtdneg, remessaOrigem);
	         imp.calcularImpostos(nunota);
	         imp.setForcarRecalculo(true);
	         imp.totalizarNota(nunota);
	         Adicionaitem add = new Adicionaitem();
	         BigDecimal vlrtotCab = add.selecionarItens(BigDecimal.valueOf(24L));
	         add.atualizarValorCAB(vlrtotCab, BigDecimal.valueOf(24L));
	      } catch (Exception var18) {
	         StringWriter sw = new StringWriter();
	         PrintWriter pw = new PrintWriter(sw);
	         var18.printStackTrace(pw);
	      }

	   }
//SOMAR OS PRODUTOS NA NOTA QUE ESTÁ SENDO LANÇADA   
//   public void somaProd(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal qtdAnterior, BigDecimal volume, BigDecimal remessaOrigem) throws Exception {
//	      BigDecimal novavariavel = qtdneg.add(qtdAnterior);
//	      ImpostosHelpper imp = new ImpostosHelpper();
//	      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
//	      JdbcWrapper jdbc = entity.getJdbcWrapper();
//	      jdbc.openSession();
//	      NativeSql query = new NativeSql(jdbc);
//	      query.setNamedParameter("NUNOTA", nunota);
//	      query.setNamedParameter("CODPROD", codprod);
//	      query.appendSql("SELECT VLRUNIT FROM TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD");
//	      ResultSet rset = query.executeQuery();
//	      if (rset.next()) {
//	         BigDecimal vlrunitario = rset.getBigDecimal("VLRUNIT");
//
//	         try {
//	            NativeSql sql = new NativeSql(jdbc);
//	            sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD");
//	            sql.setNamedParameter("NUNOTA", nunota);
//	            sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novavariavel));
//	            sql.setNamedParameter("QUANTIDADE", novavariavel);
//	            sql.setNamedParameter("CODPROD", codprod);
//	            sql.executeUpdate();
//	            imp.calcularImpostos(nunota);
//	            imp.setForcarRecalculo(true);
//	            imp.totalizarNota(nunota);
//	         } catch (Exception var18) {
//	            var18.printStackTrace();
//	         } finally {
//	            JdbcWrapper.closeSession(jdbc);
//	         }
//	      }
//
//	   }
   //VERIFICA SE O ITEM JÁ ESTAVA NA NOTA PARA SOMAR 
//   public boolean verificaNota(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal sequencia, BigDecimal apontamento, BigDecimal remessaOrigem, BigDecimal volume) throws MGEModelException {
//	      boolean resultado = false;
//	      SessionHandle hnd = null;
//	      JdbcWrapper jdbc = null;
//	      NativeSql query = null;
//	      ResultSet rset = null;
//	      System.out.println("função verificaNota");
//
//	      try {
//	         hnd = JapeSession.open();
//	         hnd.setFindersMaxRows(-1);
//	         EntityFacade entity = EntityFacadeFactory.getDWFFacade();
//	         jdbc = entity.getJdbcWrapper();
//	         jdbc.openSession();
//	         query = new NativeSql(jdbc);
//	         query.setNamedParameter("CODPROD", codprod);
//	         query.setNamedParameter("NUNOTA", nunota);
//	         query.appendSql("SELECT QTDNEG FROM SANKHYA.TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD");
//	         rset = query.executeQuery();
//	         if (rset.next()) {
//	            resultado = true;
//	            BigDecimal qtdAnterior = rset.getBigDecimal("QTDNEG");
//	            this.somaProd(nunota, codprod, qtdneg, qtdAnterior, volume, remessaOrigem);
//	            this.subtrairDoApontamento(codprod, sequencia, apontamento, qtdneg, remessaOrigem);
//	         }
//	      } catch (Exception var18) {
//	         var18.printStackTrace();
//	         MGEModelException.throwMe(var18);
//	         System.out.println("Erro ao Executar consultaPreco" + var18.getCause() + var18.getMessage());
//	      } finally {
//	         JdbcWrapper.closeSession(jdbc);
//	         JapeSession.close(hnd);
//	      }
//
//	      return resultado;
//	   }

   
   //SUBTRAI DA NOTA DE ORIGEM
   public void subtrairDoApontamento(BigDecimal codprod, BigDecimal sequencia, BigDecimal apontamento, BigDecimal qtdneg, BigDecimal remessaOrigem) throws MGEModelException {
	      SessionHandle hnd = null;
	      JdbcWrapper jdbc = null;
	      NativeSql query = null;
	      ResultSet rset = null;
	      System.out.println(" ###################achou subtrairDoApontamento ");

	      try {
	         hnd = JapeSession.open();
	         hnd.setFindersMaxRows(-1);
	         EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	         jdbc = entity.getJdbcWrapper();
	         jdbc.openSession();
	         query = new NativeSql(jdbc);
	         query.setNamedParameter("CODPROD", codprod);
	         query.setNamedParameter("SEQUENCIA", sequencia);
	         query.setNamedParameter("NUNOTA", remessaOrigem);
	         query.appendSql("SELECT QTDNEG,NUNOTA FROM SANKHYA.TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD AND SEQUENCIA = :SEQUENCIA");
	         rset = query.executeQuery();
	         if (rset.next()) {
	            BigDecimal qtdAnterior = rset.getBigDecimal("QTDNEG");
	            BigDecimal nunota = rset.getBigDecimal("NUNOTA");
	            this.subtraiProd(nunota, codprod, qtdneg, qtdAnterior, sequencia, remessaOrigem);
	            Adicionaitem add = new Adicionaitem();
	            BigDecimal vlrtotCab = add.selecionarItens(BigDecimal.valueOf(24L));
	            add.atualizarValorCAB(vlrtotCab, BigDecimal.valueOf(24L));
	         }
	      } catch (Exception var18) {
	         var18.printStackTrace();
	         MGEModelException.throwMe(var18);
	         System.out.println("Erro ao Executar consultaPreco" + var18.getCause() + var18.getMessage());
	      } finally {
	         JdbcWrapper.closeSession(jdbc);
	         JapeSession.close(hnd);
	      }

	   }
   
   public void subtraiProd(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal qtdAnterior, BigDecimal sequencia, BigDecimal remessaOrigem) throws Exception {
	      BigDecimal novavariavel = qtdAnterior.subtract(qtdneg);
	      String where = "";

	      ImpostosHelpper imp = new ImpostosHelpper();
	      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	      JdbcWrapper jdbc = entity.getJdbcWrapper();
	      jdbc.openSession();
	      NativeSql query = new NativeSql(jdbc);
	      query.setNamedParameter("CODPROD", codprod);
	      query.appendSql("SELECT VLRVENDA FROM TGFEXC, TGFTAB WHERE TGFTAB.NUTAB = TGFEXC.NUTAB AND TGFTAB.CODTAB = 6AND  DTVIGOR =  (SELECT MAX( DTVIGOR) FROM TGFTAB TAB1 WHERE TGFTAB.CODTAB = TAB1.CODTAB AND CODPROD = :CODPROD)");
	      ResultSet rset = query.executeQuery();
	      if (rset.next()) {
	         BigDecimal vlrunitario = rset.getBigDecimal("VLRVENDA");

	         try {
	            NativeSql sql = new NativeSql(jdbc);
	            sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA  AND SEQUENCIA = :SEQUENCIA");
	            sql.setNamedParameter("SEQUENCIA", sequencia);
	            sql.setNamedParameter("NUNOTA", remessaOrigem);
	            sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novavariavel));
	            sql.setNamedParameter("QUANTIDADE", novavariavel);
	            sql.setNamedParameter("CODPROD", codprod);
	            sql.executeUpdate();
	            imp.calcularImpostos(nunota);
	            imp.setForcarRecalculo(true);
	            imp.totalizarNota(nunota);
	         } catch (Exception var19) {
	            var19.printStackTrace();
	         } finally {
	            JdbcWrapper.closeSession(jdbc);
	         }
	      }

	   }
//
	   public void atualizaNotaOrigem(BigDecimal nunotaOrigem, BigDecimal qtdneg, BigDecimal qtdAnterior, BigDecimal sequencia, BigDecimal codprod, BigDecimal vlrunitario) throws Exception {
	      BigDecimal novoQTD = qtdneg.add(qtdAnterior);
	      EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	      JdbcWrapper jdbc = entity.getJdbcWrapper();
	      jdbc.openSession();

	      try {
	         NativeSql sql = new NativeSql(jdbc);
	         sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA  AND SEQUENCIA = :SEQUENCIA");
	         sql.setNamedParameter("SEQUENCIA", sequencia);
	         sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novoQTD));
	         sql.setNamedParameter("QUANTIDADE", novoQTD);
	         sql.setNamedParameter("CODPROD", codprod);
	         sql.setNamedParameter("NUNOTA", nunotaOrigem);
	         sql.executeUpdate();
	      } catch (Exception var14) {
	         var14.printStackTrace();
	      } finally {
	         JdbcWrapper.closeSession(jdbc);
	      }

	   }
   
   //CONFIRMAR NOTA 
//   public void confirmaPedidoSnk(final BigDecimal nuNota) throws MGEModelException {
//	      SessionHandle hnd = null;
//
//	      try {
//	         hnd = JapeSession.open();
//	         hnd.execWithTX(new TXBlock() {
//	            public void doWithTx() throws Exception {
//	               AuthenticationInfo authenticationInfo = new AuthenticationInfo("SUP", BigDecimal.ZERO, BigDecimal.ZERO, 0);
//	               authenticationInfo.makeCurrent();
//	               AuthenticationInfo.getCurrent();
//	               BarramentoRegra barramentoConfirmacao = BarramentoRegra.build(CentralFaturamento.class, "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
//	               barramentoConfirmacao.setValidarSilencioso(true);
//	               ConfirmacaoNotaHelper.confirmarNota(nuNota, barramentoConfirmacao);
//	            }
//	         });
//	         JapeSession.close(hnd);
//	      } catch (Exception var4) {
//	         var4.printStackTrace();
//	         MGEModelException.throwMe(var4);
//	      }
//
//	   }
//
//	   public void GerarNota(final BigDecimal nunota, BigDecimal top) throws Exception {
//	      final CentralFaturamento cent = new CentralFaturamento();
//	      cent.abreCabecalhoNotaOrigem(nunota, false);
//	      final ConfiguracaoFaturamento cf = cent.getConfiguracaoFaturamento();
//
//	      try {
//	         JapeWrapper codtipoperDao = JapeFactory.dao("TipoOperacao");
//	         DynamicVO codtipoperVo = codtipoperDao.findOne("CODTIPOPER=?", new Object[]{top});
//	         cf.setTipMovDest(codtipoperVo.asString("TIPMOV"));
//	         cf.setUsaTopDestino(false);
//	         cf.isIncluirNotaPendente();
//	         cf.setSerie("1");
//	         cf.setConfirmarNota(true);
//	         cf.setCodTipOper(top);
//	         final Collection<BigDecimal> notasNunota = new ArrayList();
//	         notasNunota.add(nunota);
//	         final SessionHandle hnd = JapeSession.open();
//	         hnd.execWithTX(new TXBlock() {
//	            public void doWithTx() throws Exception {
//	               cent.getConfiguracaoFaturamento().getTipMovDest();
//	               Map<BigDecimal, BigDecimal> m = new HashMap();
//	               m.put(nunota, BigDecimal.valueOf(0L));
//	               AuthenticationInfo auth = new AuthenticationInfo("SUP", BigDecimal.ZERO, BigDecimal.ZERO, 0);
//	               auth.makeCurrent();
//	               FaturamentoHelper.faturar(ServiceContext.getCurrent(), hnd, cf, notasNunota, m);
//	            }
//	         });
//	         JapeSession.close(hnd);
//	      } catch (Exception var9) {
//	         var9.printStackTrace();
//	         throw new Exception("Erro ao faturar pedido: " + var9);
//	      }
//	   }

}