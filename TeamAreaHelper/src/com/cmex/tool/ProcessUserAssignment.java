package com.cmex.tool;


import com.ibm.team.process.client.IClientProcess;
import com.ibm.team.process.client.IProcessItemService;
import com.ibm.team.process.common.IProcessArea;
import com.ibm.team.process.common.IProcessItem;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.IRole;
import com.ibm.team.process.common.IRole2;
import com.ibm.team.process.common.ITeamArea;
import com.ibm.team.process.common.ITeamAreaHandle;
import com.ibm.team.process.common.ITeamAreaHierarchy;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.foundation.api.common.progress.IProgressMonitor;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;

public class ProcessUserAssignment {
	public enum processType{
		PA,TA,PTA,CTA,STA
	}

	public void  assignUsers(RtcConfigValueHolder configValueHolder, List<DataVO> contributorInfoList) throws TeamRepositoryException{
		Iterator<DataVO> listIterator = contributorInfoList.listIterator();
		IProcessItemService itemService = (IProcessItemService)configValueHolder.getRtcRepository().getClientLibrary(IProcessItemService.class);
		IProjectArea projectAreaInstance =  (IProjectArea)configValueHolder.getPojectAreaInstance().getWorkingCopy();
		//	(IProjectArea)itemService.getMutableCopy(configValueHolder.getPojectAreaInstance());
		ITeamRepository teamRepository = configValueHolder.getRtcRepository();
		List<ITeamAreaHandle> teamAreas =  projectAreaInstance.getTeamAreas(); 
		Iterator<ITeamAreaHandle> teamAreaIterator = teamAreas.iterator();
		ITeamAreaHierarchy teamHierarchy = projectAreaInstance.getTeamAreaHierarchy();
		Map<IContributor,IRole[]> teamAreaDetailHolder = new HashMap<IContributor, IRole[]>();
		Map<IContributor,IRole[]> parentTeamAreaDetailHolder = new HashMap<IContributor, IRole[]>();
		Map<IContributor,IRole[]> childTeamAreaDetailHolder = new HashMap<IContributor, IRole[]>();
		Map<IContributor,IRole[]> specificTeamAreaDetailHolder = new HashMap<IContributor, IRole[]>();
		Map<IContributor,List<String>> staContributorMap=new HashMap<IContributor, List<String>>();

		while(listIterator.hasNext()){
			DataVO contributorData = listIterator.next();
			System.out.println("===========contributorData==="+contributorData.getFlag()+"[value of ]"+processType.valueOf(contributorData.getFlag().toUpperCase()));
			switch (processType.valueOf(contributorData.getFlag().toUpperCase())){
			case PA :
				projectAreaInstance.addMember(contributorData.getContributor());
				projectAreaInstance.addRoleAssignments(contributorData.getContributor(), getRolesForUSer(configValueHolder,contributorData.getRoles()));
				break;
			case TA :
				teamAreaDetailHolder.put(contributorData.getContributor(), getRolesForUSer(configValueHolder,contributorData.getRoles()));
				break;
			case PTA :
				parentTeamAreaDetailHolder.put(contributorData.getContributor(), getRolesForUSer(configValueHolder,contributorData.getRoles()));
				break;
			case CTA :
				childTeamAreaDetailHolder.put(contributorData.getContributor(), getRolesForUSer(configValueHolder,contributorData.getRoles()));
				break;
			case STA :
				specificTeamAreaDetailHolder.put(contributorData.getContributor(), getRolesForUSer(configValueHolder,contributorData.getRoles()));
				staContributorMap.put(contributorData.getContributor(), contributorData.getTeamAreas());
				break;

			}
		}
			if(childTeamAreaDetailHolder.size()>0){
				System.out.println("=======childTeamAreaDetailHolder===== "+childTeamAreaDetailHolder.size());
				while(teamAreaIterator.hasNext()){
					Set<ITeamAreaHandle> childTeamAreaHandles = teamHierarchy.getChildren((ITeamAreaHandle)teamAreaIterator.next());
					for(ITeamAreaHandle childHandle : childTeamAreaHandles){
						ITeamArea childTeamArea = (ITeamArea)teamRepository.itemManager()
								.fetchCompleteItem(childHandle, 0, null);
						childTeamArea = (ITeamArea)itemService.getMutableCopy(childTeamArea);
						Iterator<IContributor> teamAreaContributor =childTeamAreaDetailHolder.keySet().iterator();
						while(teamAreaContributor.hasNext()){
							IContributor iContributer = teamAreaContributor.next();
							childTeamArea.addMember((IContributorHandle)iContributer.getItemHandle());
							childTeamArea.addRoleAssignments((IContributorHandle)iContributer.getItemHandle(), childTeamAreaDetailHolder.get(iContributer));
						}
					//	itemService.save(new IProcessItem[] { childTeamArea }, new NullProgressMonitor());
					}


				}
			}


			if(parentTeamAreaDetailHolder.size()>0){
				System.out.println("=======parentTeamAreaDetailHolder===== "+parentTeamAreaDetailHolder.size());
				while(teamAreaIterator.hasNext()){
					ITeamAreaHandle parentHandle = teamHierarchy.getParent((ITeamAreaHandle)teamAreaIterator.next());
					ITeamArea parentTeamArea = (ITeamArea)teamRepository.itemManager()
							.fetchCompleteItem(parentHandle, 0, null);
					parentTeamArea = (ITeamArea)itemService.getMutableCopy(parentTeamArea);
					Iterator<IContributor> teamAreaContributor =parentTeamAreaDetailHolder.keySet().iterator();
					while(teamAreaContributor.hasNext()){
						IContributor iContributer = teamAreaContributor.next();
						parentTeamArea.addMember((IContributorHandle)iContributer.getItemHandle());
						parentTeamArea.addRoleAssignments((IContributorHandle)iContributer.getItemHandle(), parentTeamAreaDetailHolder.get(iContributer));
					}
					//itemService.save(new IProcessItem[] { parentTeamArea }, new NullProgressMonitor());
				}
			}
			
			if(teamAreaDetailHolder.size()>0){
				System.out.println("=======teamAreaDetailHolder========="+childTeamAreaDetailHolder.size());
				while(teamAreaIterator.hasNext()){
					ITeamArea teamArea = (ITeamArea)teamRepository.itemManager()
							.fetchCompleteItem((ITeamAreaHandle)teamAreaIterator.next(), 0, null);
					teamArea = (ITeamArea)itemService.getMutableCopy(teamArea);					
					Iterator<IContributor> teamAreaContributor =teamAreaDetailHolder.keySet().iterator();
					while(teamAreaContributor.hasNext()){
						IContributor iContributer = teamAreaContributor.next();
						teamArea.addMember((IContributorHandle)iContributer.getItemHandle());
						teamArea.addRoleAssignments((IContributorHandle)iContributer.getItemHandle(), teamAreaDetailHolder.get(iContributer));
					}
				//	itemService.save(new IProcessItem[] { teamArea }, 	new NullProgressMonitor());
				}
			}
			
			if(specificTeamAreaDetailHolder.size()>0){
				System.out.println("=======teamAreaDetailHolder========="+specificTeamAreaDetailHolder.size());
				while(teamAreaIterator.hasNext()){
					ITeamArea teamArea = (ITeamArea)teamRepository.itemManager()
							.fetchCompleteItem((ITeamAreaHandle)teamAreaIterator.next(), 0, null);
					teamArea = (ITeamArea)itemService.getMutableCopy(teamArea);					
					Iterator<IContributor> teamAreaContributor =specificTeamAreaDetailHolder.keySet().iterator();
					while(teamAreaContributor.hasNext()){
						IContributor iContributer = teamAreaContributor.next();
						//if current team area present in list of specific team areas from input 
						if(staContributorMap.get(iContributer).contains(teamArea.getName())){
							System.out.println("======Adding member for specfic team areas======");
							teamArea.addMember((IContributorHandle)iContributer.getItemHandle());
							teamArea.addRoleAssignments((IContributorHandle)iContributer.getItemHandle(), specificTeamAreaDetailHolder.get(iContributer));
						}
					}
				//	itemService.save(new IProcessItem[] { teamArea }, 	new NullProgressMonitor());
				}
			}
			System.out.println("=======Saving=========");

			itemService.save(new IProcessItem[] { projectAreaInstance }, 
					new NullProgressMonitor());
		}

	
	private  IRole[] getRolesForUSer(RtcConfigValueHolder configValueHolder, List<String> roles) throws TeamRepositoryException{
		IRole[] assignroleArray = new IRole[roles.size()];
		for (int i = 0; i < roles.size(); i++) {
			assignroleArray[i] = getRole(configValueHolder.getPojectAreaInstance(), (String)roles.get(i), null);
		}
		return  assignroleArray;
	}

	private IRole getRole(IProcessArea area, String roleID, IProgressMonitor monitor)
			throws TeamRepositoryException
	{
		ITeamRepository repo = (ITeamRepository)area.getOrigin();
		IProcessItemService service = (IProcessItemService)repo
				.getClientLibrary(IProcessItemService.class);
		IClientProcess clientProcess = service.getClientProcess(area, null);
		IRole[] availableRoles = clientProcess.getRoles(area, null);
		for (int i = 0; i < availableRoles.length; i++)
		{
			IRole2 role = (IRole2)availableRoles[i];
			System.out.println("===========" + role.getRoleName());
			if (role.getRoleName().equalsIgnoreCase(roleID)) {
				return role;
			}
		}
		throw new IllegalArgumentException("Couldn't find roles");
	}
}


