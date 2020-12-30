package nl.rivm.screenit.main.web.gebruiker.dashboard;

/*-
 * ========================LICENSE_START=================================
 * screenit-web
 * %%
 * Copyright (C) 2012 - 2020 Facilitaire Samenwerking Bevolkingsonderzoek
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * =========================LICENSE_END==================================
 */

import java.util.Iterator;
import java.util.List;

import nl.rivm.screenit.model.SortState;
import nl.rivm.screenit.model.dashboard.DashboardStatus;
import nl.rivm.screenit.model.logging.LogRegel;
import nl.rivm.screenit.service.LogService;
import nl.topicuszorg.wicket.hibernate.util.ModelUtil;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.google.common.primitives.Ints;

public class DashboardDataProvider extends SortableDataProvider<LogRegel, String>
{

	private static final long serialVersionUID = 1L;

	@SpringBean
	private LogService logService;

	private final IModel<DashboardStatus> criteria;

	public DashboardDataProvider(IModel<DashboardStatus> criteria)
	{
		Injector.get().inject(this);
		setSort("logRegel.gebeurtenisDatum", SortOrder.DESCENDING);
		this.criteria = criteria;
	}

	@Override
	public Iterator<? extends LogRegel> iterator(long first, long count)
	{
		List<LogRegel> logregels = logService.getLogRegelsVanDashboard(ModelUtil.nullSafeGet(criteria), Ints.checkedCast(first), Ints.checkedCast(count),
			new SortState<>(getSort().getProperty(), getSort().isAscending()));
		return logregels.iterator();
	}

	@Override
	public long size()
	{
		return logService.countLogRegelsVanDashboard(ModelUtil.nullSafeGet(criteria));
	}

	@Override
	public IModel<LogRegel> model(LogRegel object)
	{

		return ModelUtil.sModel(object);
	}

	@Override
	public void detach()
	{
		ModelUtil.nullSafeDetach(criteria);
	}

}