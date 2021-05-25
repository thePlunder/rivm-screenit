
package nl.rivm.screenit.model.verwerkingverslag;

/*-
 * ========================LICENSE_START=================================
 * screenit-base
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import nl.topicuszorg.hibernate.object.model.AbstractHibernateObject;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(schema = "colon")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "screenit.cache")
public class SelectieRapportage extends AbstractHibernateObject
{
	@OneToMany(mappedBy = "rapportage", cascade = CascadeType.ALL)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "screenit.cache")
	private List<SelectieRapportageEntry> entries = new ArrayList<>();

	@OneToMany(mappedBy = "rapportage", cascade = CascadeType.ALL)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "screenit.cache")
	private List<SelectieRapportageGewijzigdGebiedEntry> gewijzigdeGebieden = new ArrayList<>();

	@OneToMany(mappedBy = "rapportage", cascade = CascadeType.ALL)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "screenit.cache")
	private List<SelectieRapportageProjectGroepEntry> projectGroepen = new ArrayList<>();

	@Temporal(TemporalType.TIMESTAMP)
	private Date datumVerwerking;

	public List<SelectieRapportageEntry> getEntries()
	{
		return entries;
	}

	public void setEntries(List<SelectieRapportageEntry> entries)
	{
		this.entries = entries;
	}

	public Date getDatumVerwerking()
	{
		return datumVerwerking;
	}

	public void setDatumVerwerking(Date datumVerwerking)
	{
		this.datumVerwerking = datumVerwerking;
	}

	public List<SelectieRapportageGewijzigdGebiedEntry> getGewijzigdeGebieden()
	{
		return gewijzigdeGebieden;
	}

	public void setGewijzigdeGebieden(List<SelectieRapportageGewijzigdGebiedEntry> gewijzigdeGebieden)
	{
		this.gewijzigdeGebieden = gewijzigdeGebieden;
	}

	public List<SelectieRapportageProjectGroepEntry> getProjectGroepen()
	{
		return projectGroepen;
	}

	public void setProjectGroepen(List<SelectieRapportageProjectGroepEntry> projectGroepen)
	{
		this.projectGroepen = projectGroepen;
	}
}
