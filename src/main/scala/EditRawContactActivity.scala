package org.positronicnet.sample.contacts

import org.positronicnet.ui._
import org.positronicnet.notifications.Actions._
import org.positronicnet.content.PositronicContentResolver

import android.util.Log
import android.os.Bundle

class EditRawContactActivity
  extends PositronicActivity( layoutResourceId = R.layout.edit_contact )
  with TypedViewHolder
  with ActivityResultDispatch           // for photo edit widgetry
{
  onCreate {
    useAppFacility( PositronicContentResolver )
    useAppFacility( Res )               // stash a copy of the Resources

    useOptionsMenuResource( R.menu.edit_contact_menu )
    onOptionsItemSelected( R.id.save_raw_contact ) { doSave }
  }

  // Management of our edit state across the Activity lifecycle,
  // including suspend/recreate cycles (due to orientation changes,
  // or whatever else).

  var state: ContactEditState = null

  override def createInstanceState = {

    // Have no saved instance state.  Create it.

    val rawContact = 
      getIntent.getSerializableExtra( "raw_contact" ).asInstanceOf[ RawContact ]

    if (rawContact.isNewRecord) {
      this.bindState( new ContactEditState( rawContact, Seq.empty ))
    }
    else {
      rawContact.data ! Fetch { data => 
        this.bindState( new ContactEditState( rawContact, data ))
      }
    }
  }

  override def saveInstanceState( b: Bundle ) = {
    syncState
    b.putSerializable( "contact_edit_state", this.state )
  }

  override def restoreInstanceState( b: Bundle ) = {
    val state = b.getSerializable( "contact_edit_state" )
    this.bindState( state.asInstanceOf[ ContactEditState ] )
  }

  // Loading a state into our editor widgets

  def bindState( state: ContactEditState ) = {
    this.state = state
    findView( TR.rawContactEditor ).bindState( state )
  }

  // Updating the state from what's displayed in the editor widgets.

  def syncState = findView( TR.rawContactEditor ).updateState

  // Doing a save

  def doSave = {
    syncState
    state.logIt
    PositronicContentResolver ! state.saveBatch.onSuccess{ finish }.onFailure{ 
      toastShort("Error saving; see log") }
  }
}




