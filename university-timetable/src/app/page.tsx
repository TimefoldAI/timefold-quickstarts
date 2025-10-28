'use client';

import { useState, useEffect } from 'react';
import { Timetable } from '@/types/timetable';
import { timetableApi } from '@/lib/api';

export default function Home() {
  const [timetable, setTimetable] = useState<Timetable | null>(null);
  const [demoDataList, setDemoDataList] = useState<string[]>([]);
  const [selectedDemoData, setSelectedDemoData] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    loadDemoDataList();
  }, []);

  const loadDemoDataList = async () => {
    try {
      setLoading(true);
      setError('');
      console.log('🔄 Loading demo data list...');
      const data = await timetableApi.getDemoDataList();
      console.log('✅ Demo data list loaded:', data);
      setDemoDataList(data);
      if (data.length > 0) {
        setSelectedDemoData(data[0]);
        await loadDemoData(data[0]);
      }
    } catch (error) {
      console.error('❌ Failed to load demo data list:', error);
      setError('Failed to load demo data list');
    } finally {
      setLoading(false);
    }
  };

  const loadDemoData = async (demoDataId: string) => {
    try {
      setLoading(true);
      setError('');
      console.log('🔄 Loading demo data:', demoDataId);
      const data = await timetableApi.getDemoData(demoDataId);
      console.log('✅ Demo data loaded successfully:', data);
      console.log('📊 Data structure:', {
        name: data.name,
        timeslots: data.timeslots?.length,
        rooms: data.rooms?.length,
        teachers: data.teachers?.length,
        lessons: data.lessons?.length,
        studentGroups: data.studentGroups?.length
      });
      setTimetable(data);
    } catch (error) {
      console.error('❌ Failed to load demo data:', error);
      setError(`Failed to load demo data: ${demoDataId}`);
    } finally {
      setLoading(false);
    }
  };

  // Debug current state
  console.log('🔍 CURRENT STATE:', {
    loading,
    error,
    hasTimetable: !!timetable,
    timetableName: timetable?.name,
    demoDataList,
    selectedDemoData
  });

  if (loading) {
    return (
      <div style={{ 
        minHeight: '100vh', 
        backgroundColor: '#f9fafb', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center',
        fontFamily: 'Arial, sans-serif'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '18px', fontWeight: '500', color: '#111827' }}>
            Loading timetable data...
          </div>
          <div style={{ fontSize: '14px', color: '#6b7280', marginTop: '8px' }}>
            Please wait while we load the schedule data
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ 
        minHeight: '100vh', 
        backgroundColor: '#f9fafb', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center',
        fontFamily: 'Arial, sans-serif'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '18px', fontWeight: '500', color: '#dc2626' }}>
            Error loading data
          </div>
          <div style={{ fontSize: '14px', color: '#6b7280', marginTop: '8px' }}>
            {error}
          </div>
          <button
            onClick={loadDemoDataList}
            style={{
              marginTop: '16px',
              padding: '8px 16px',
              backgroundColor: '#3b82f6',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={{ 
      minHeight: '100vh', 
      backgroundColor: '#f9fafb',
      fontFamily: 'Arial, sans-serif'
    }}>
      {/* Status Banner */}
      <div style={{
        backgroundColor: '#d1fae5',
        borderBottom: '1px solid #a7f3d0',
        padding: '8px',
        textAlign: 'center',
        fontSize: '14px',
        color: '#065f46'
      }}>
        🟢 App Loaded | Timetable: {timetable?.name || 'None'} | 
        Lessons: {timetable?.lessons?.length || 0} |
        Rooms: {timetable?.rooms?.length || 0}
      </div>

      {/* Header */}
      <header style={{
        backgroundColor: 'white',
        boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
        borderBottom: '1px solid #e5e7eb'
      }}>
        <div style={{
          maxWidth: '1200px',
          margin: '0 auto',
          padding: '16px 20px'
        }}>
          <h1 style={{
            fontSize: '24px',
            fontWeight: 'bold',
            color: '#111827',
            margin: 0
          }}>
            University Timetable Scheduler
          </h1>
        </div>
      </header>

      {/* Main Content */}
      <main style={{
        maxWidth: '1200px',
        margin: '0 auto',
        padding: '32px 20px'
      }}>
        {/* Control Panel */}
        <div style={{
          backgroundColor: 'white',
          borderRadius: '8px',
          boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
          border: '1px solid #e5e7eb',
          padding: '24px',
          marginBottom: '32px'
        }}>
          <div style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '16px'
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '16px',
              flexWrap: 'wrap'
            }}>
              <div>
                <label style={{
                  display: 'block',
                  fontSize: '14px',
                  fontWeight: '500',
                  color: '#374151',
                  marginBottom: '4px'
                }}>
                  Demo Data
                </label>
                <select
                  value={selectedDemoData}
                  onChange={(e) => {
                    setSelectedDemoData(e.target.value);
                    loadDemoData(e.target.value);
                  }}
                  style={{
                    display: 'block',
                    width: '200px',
                    borderRadius: '6px',
                    border: '1px solid #d1d5db',
                    padding: '8px 12px',
                    fontSize: '14px'
                  }}
                >
                  {demoDataList.map((data) => (
                    <option key={data} value={data}>
                      {data.charAt(0).toUpperCase() + data.slice(1).toLowerCase()}
                    </option>
                  ))}
                </select>
              </div>

              <button
                onClick={() => loadDemoData(selectedDemoData)}
                style={{
                  padding: '8px 16px',
                  backgroundColor: '#3b82f6',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  fontSize: '14px',
                  fontWeight: '500',
                  cursor: 'pointer'
                }}
              >
                Refresh Data
              </button>
            </div>
          </div>
        </div>

        {/* Timetable Information */}
        {timetable ? (
          <div style={{
            backgroundColor: 'white',
            borderRadius: '8px',
            boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
            border: '1px solid #e5e7eb',
            padding: '24px'
          }}>
            <h2 style={{
              fontSize: '18px',
              fontWeight: '600',
              color: '#111827',
              marginBottom: '16px'
            }}>
              📅 Timetable: {timetable.name}
            </h2>

            {/* Statistics */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: '16px',
              marginBottom: '24px'
            }}>
              <div style={{
                padding: '12px',
                backgroundColor: '#f3f4f6',
                borderRadius: '6px',
                textAlign: 'center'
              }}>
                <div style={{ fontSize: '14px', color: '#6b7280' }}>Timeslots</div>
                <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#111827' }}>
                  {timetable.timeslots?.length || 0}
                </div>
              </div>
              <div style={{
                padding: '12px',
                backgroundColor: '#f3f4f6',
                borderRadius: '6px',
                textAlign: 'center'
              }}>
                <div style={{ fontSize: '14px', color: '#6b7280' }}>Rooms</div>
                <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#111827' }}>
                  {timetable.rooms?.length || 0}
                </div>
              </div>
              <div style={{
                padding: '12px',
                backgroundColor: '#f3f4f6',
                borderRadius: '6px',
                textAlign: 'center'
              }}>
                <div style={{ fontSize: '14px', color: '#6b7280' }}>Teachers</div>
                <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#111827' }}>
                  {timetable.teachers?.length || 0}
                </div>
              </div>
              <div style={{
                padding: '12px',
                backgroundColor: '#f3f4f6',
                borderRadius: '6px',
                textAlign: 'center'
              }}>
                <div style={{ fontSize: '14px', color: '#6b7280' }}>Lessons</div>
                <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#111827' }}>
                  {timetable.lessons?.length || 0}
                </div>
              </div>
            </div>

            {/* Sample Lessons */}
            {timetable.lessons && timetable.lessons.length > 0 && (
              <div>
                <h3 style={{
                  fontSize: '16px',
                  fontWeight: '600',
                  color: '#111827',
                  marginBottom: '12px'
                }}>
                  Sample Lessons ({timetable.lessons.length} total):
                </h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {timetable.lessons.slice(0, 10).map((lesson, index) => (
                    <div key={lesson.id || index} style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      padding: '8px 12px',
                      backgroundColor: '#f9fafb',
                      borderRadius: '4px',
                      border: '1px solid #e5e7eb'
                    }}>
                      <span style={{ fontWeight: '500' }}>{lesson.subject}</span>
                      <span style={{ fontSize: '14px', color: '#6b7280' }}>
                        {lesson.teacher?.name} → {lesson.studentGroup?.name}
                      </span>
                    </div>
                  ))}
                  {timetable.lessons.length > 10 && (
                    <div style={{
                      textAlign: 'center',
                      fontSize: '14px',
                      color: '#6b7280',
                      padding: '8px'
                    }}>
                      ... and {timetable.lessons.length - 10} more lessons
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        ) : (
          <div style={{
            backgroundColor: 'white',
            borderRadius: '8px',
            boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
            border: '1px solid #e5e7eb',
            padding: '24px',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '16px', color: '#6b7280' }}>
              No timetable data loaded. Please select a demo dataset.
            </div>
          </div>
        )}
      </main>
    </div>
  );
}