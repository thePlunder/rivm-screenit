package nl.rivm.screenit.model.algemeen;

/*-
 * ========================LICENSE_START=================================
 * screenit-base
 * %%
 * Copyright (C) 2012 - 2022 Facilitaire Samenwerking Bevolkingsonderzoek
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nl.rivm.screenit.model.enums.Bevolkingsonderzoek;

public class BezwaarGroupViewWrapper implements Serializable
{

	private static final long serialVersionUID = 1L;

	private String key;

	private Bevolkingsonderzoek bevolkingsonderzoek;

	private List<BezwaarViewWrapper> bezwaren = new ArrayList<BezwaarViewWrapper>();

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public Bevolkingsonderzoek getBevolkingsonderzoek()
	{
		return bevolkingsonderzoek;
	}

	public void setBevolkingsonderzoek(Bevolkingsonderzoek bevolkingsonderzoek)
	{
		this.bevolkingsonderzoek = bevolkingsonderzoek;
	}

	public List<BezwaarViewWrapper> getBezwaren()
	{
		return bezwaren;
	}

	public void setBezwaren(List<BezwaarViewWrapper> bezwaren)
	{
		this.bezwaren = bezwaren;
	}

}
