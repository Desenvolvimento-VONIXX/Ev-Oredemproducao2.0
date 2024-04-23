package eventoop;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import baop.Lancapedido;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
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
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class Ev_LogApontamento implements EventoProgramavelJava{

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent evt) throws Exception {
	      DynamicVO target = (DynamicVO)evt.getVo();
	      BigDecimal nunota = target.asBigDecimal("NUNOTA");
	      BigDecimal codprod = target.asBigDecimal("CODPROD");
	      BigDecimal qtdneg = target.asBigDecimal("QTDNEG");
	      BigDecimal sequencia = target.asBigDecimal("SEQUENCIA");
	      BigDecimal apontamento = target.asBigDecimal("APONTAMENTO");
	      String codvol = target.asString("CODVOL");
	      BigDecimal vlrunit = target.asBigDecimal("VLRUNIT");
	      BigDecimal vlrtotal = target.asBigDecimal("VLRTOT");
	      BigDecimal volume = target.asBigDecimal("VOLUME");
	      BigDecimal remessaOrigem = target.asBigDecimal("AD_REMESSAORIGEM");
	      String faturar = target.asString("FATURAR");
	      Utilitario_HTML lp = new Utilitario_HTML();
	      
<<<<<<< HEAD
	      BigDecimal nunpedevox= buscarnunpedevox(remessaOrigem,codprod,sequencia);
	      if(nunpedevox==null) {
	    	 boolean somaOuLanca = lp.verificaNota(nunota, codprod, qtdneg, sequencia, apontamento,remessaOrigem,volume);
	      
	      
=======
	      //Verifica se na nota que foi lançada já tem o cod prod, se sim soma a qtdneg
	      boolean somaOuLanca = lp.verificaNota(nunota, codprod, qtdneg, sequencia, apontamento,remessaOrigem,volume);
>>>>>>> bff807742d4941d436407bdb1f62db3b605f36d7
	      if (!somaOuLanca) {
	         
	         
	         //Lança o produto na nota gerada
	        	 lp.additensLog(nunota, codprod, qtdneg, codvol, vlrunit, vlrtotal, remessaOrigem, sequencia, apontamento,volume, nunpedevox);
	         
	         
	      }
	      }else {
	    	  boolean somaOuLanca = lp.verificaNotaEVOX(nunota, codprod, qtdneg, sequencia, apontamento,remessaOrigem,volume,nunpedevox);
	    	  
	    	  if (!somaOuLanca) {
	 	         
	 	         
		        	 lp.additensLog(nunota, codprod, qtdneg, codvol, vlrunit, vlrtotal, remessaOrigem, sequencia, apontamento,volume, nunpedevox);
		         
		         
		      }
	      }
		    //verifica se tem que lançar BOMBONA
          if(volume.intValue() ==6) {
        	  //lp.adicionaBombona(nunota,qtdneg,remessaOrigem);
          }
          

//	      if ("S".equals(faturar)) {
//	         Utilitarios ut = new Utilitarios();
//	         ut.confirmaPedidoSnk(nunota);
//	      }

	   }
        		
    			
		
	
	

	private BigDecimal buscarnunpedevox(BigDecimal remessaOrigem, BigDecimal codprod, BigDecimal sequencia) throws SQLException, MGEModelException {
		 EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	 		JdbcWrapper jdbc = entity.getJdbcWrapper();
	 		jdbc.openSession();
	 		BigDecimal nunpedevox=null;
	 		
		
		try {
		 String sql1 = "SELECT AD_NUNPEDEVOX FROM TGFITE WHERE CODPROD=:CODPROD AND NUNOTA=:NUNOTA AND SEQUENCIA=:SEQUENCIA";
     NativeSql query1 = new NativeSql(jdbc);
     query1.setNamedParameter("NUNOTA", remessaOrigem);
     query1.setNamedParameter("CODPROD", codprod);
     query1.setNamedParameter("SEQUENCIA", sequencia);
     query1.appendSql(sql1);
     ResultSet rset1 = query1.executeQuery();

     if (rset1.next()) {
     	nunpedevox=rset1.getBigDecimal("AD_NUNPEDEVOX");
     }
	       

		} catch (Exception e) {
			MGEModelException.throwMe(e);
		}

		finally {
			JdbcWrapper.closeSession(jdbc);
		}
		return nunpedevox;
		
		
	}

	@Override
	public void afterUpdate(PersistenceEvent evt) throws Exception {

		DynamicVO target = (DynamicVO) evt.getVo();
		BigDecimal nunota = target.asBigDecimal("NUNOTA");
		BigDecimal nunotaOrigem = target.asBigDecimal("AD_REMESSAORIGEM");
		BigDecimal sequencia = target.asBigDecimal("SEQUENCIA");
		BigDecimal qtdneg = target.asBigDecimal("QTDNEG");
		BigDecimal codprod = target.asBigDecimal("CODPROD");
		BigDecimal vlrunit = target.asBigDecimal("VLRUNIT");
		String codvol = target.asString("CODVOL");
		BigDecimal vlrtotal = target.asBigDecimal("VLRTOT");
		BigDecimal volume = target.asBigDecimal("VOLUME");
		
		
		if(nunotaOrigem.intValue() == 24) {
			//verificar se foi lançado na nota de origem
			boolean retornou = false;
			
			JdbcWrapper jdbc = null;
			NativeSql query = null;
			ResultSet rset = null;
			Utilitario_HTML lp = new Utilitario_HTML();
			
			try {
				retornou = true;
				EntityFacade entity = EntityFacadeFactory.getDWFFacade();
				jdbc = entity.getJdbcWrapper();
				jdbc.openSession();
				query = new NativeSql(jdbc);
				
				query.setNamedParameter("SEQUENCIA", sequencia);
				query.setNamedParameter("NUNOTA", nunotaOrigem);
				query.appendSql("SELECT QTDNEG,AD_NUNPEDEVOX FROM TGFITE WHERE NUNOTA = :NUNOTA AND SEQUENCIA = :SEQUENCIA");
				rset = query.executeQuery();
				
				if (rset.next()) {
					BigDecimal nunpedevox= rset.getBigDecimal("AD_NUNPEDEVOX");
					BigDecimal qtdAnterior = rset.getBigDecimal("QTDNEG");
					
					if(nunpedevox==null) {
						lp.atualizaNotaOrigem(nunotaOrigem,qtdneg,qtdAnterior,sequencia,codprod,vlrunit);
					}else {
						lp.atualizaNotaOrigemEVOX(nunotaOrigem,qtdneg,qtdAnterior,sequencia,codprod,vlrunit,nunpedevox);
					}
					
					Adicionaitem add = new Adicionaitem();
					BigDecimal vlrtotCab=add.selecionarItens(BigDecimal.valueOf(24));
					add.atualizarValorCAB(vlrtotCab, BigDecimal.valueOf(24));
					//util.somaProd(nunot,codprod,qtdneg,qtdAnterior);
					System.out.println(" ###################achou qtdneg da nota origem TESTE = "+qtdAnterior);
					
				}

			} catch (Exception e) {
				e.printStackTrace();
				MGEModelException.throwMe(e);
				System.out.println("Erro ao Executar consultaPreco" + e.getCause() + e.getMessage());
			} finally {
				JdbcWrapper.closeSession(jdbc);
				
			}
			
			if(!retornou) {

				lp.additensLog(nunotaOrigem, codprod, qtdneg, codvol, vlrunit, vlrtotal, null, null, null,null,null);
			}
	        if(volume.intValue() ==6) {
	      	  lp.adicionaBombona(nunotaOrigem,qtdneg,null);
	        }
		}

	

	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(PersistenceEvent evt) throws Exception {
			
	}

}
