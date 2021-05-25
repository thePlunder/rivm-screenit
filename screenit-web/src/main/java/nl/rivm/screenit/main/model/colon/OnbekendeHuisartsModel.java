package nl.rivm.screenit.main.model.colon;

/*-
 * ========================LICENSE_START=================================
 * screenit-web
 * %%
 * Copyright (C) 2012 - 2021 Facilitaire Samenwerking Bevolkingsonderzoek
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

import nl.rivm.screenit.main.model.BaseHuisartsModel;
import nl.rivm.screenit.model.OnbekendeHuisarts;
import nl.rivm.screenit.util.AdresUtil;

public class OnbekendeHuisartsModel extends BaseHuisartsModel<OnbekendeHuisarts>
{

	public OnbekendeHuisartsModel(OnbekendeHuisarts huisarts)
	{
		super(huisarts);
	}

	@Override
	public String getHuisartsNaam()
	{
		return getHuisarts().getHuisartsNaam();
	}

	@Override
	public String getPraktijkNaam()
	{
		return getHuisarts().getPraktijkNaam();
	}

	@Override
	public String getPraktijkAdres()
	{
		return AdresUtil.getOnbekendeHuisartsAdres(getHuisarts());
	}

	@Override
	public String getHuisartsAgb()
	{
		return null;
	}

	@Override
	public String getWeergaveNaam()
	{
		return null;
	}

	@Override
	public String getKlantnummer()
	{
		return null;
	}

	@Override
	public String getEdiadres()
	{
		return null;
	}

	@Override
	public String getCommunicatieadres()
	{
		return null;
	}

	@Override
	public boolean isVerwijderd()
	{
		return false;
	}
}
