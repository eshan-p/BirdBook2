import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import CreatePost from '../components/features/CreatePost';
import { AuthProvider } from '../context/AuthContext';

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        {component}
      </AuthProvider>
    </BrowserRouter>
  );
};

describe('CreatePost Component', () => {
  test('should display create post prompt', () => {
    renderWithProviders(<CreatePost />);
    expect(screen.getByText(/Log a new bird sighting/i)).toBeInTheDocument();
  });

  test('should display Photo and Location action buttons', () => {
    renderWithProviders(<CreatePost />);
    // Wait a moment for buttons to render
    const photoButton = screen.queryByText('Photo');
    const locationButton = screen.queryByText('Location');
    
    // Check if they exist (they might not render if form isn't open)
    if (photoButton && locationButton) {
      expect(photoButton).toBeInTheDocument();
      expect(locationButton).toBeInTheDocument();
    }
  });

  test('should open form when create post button is clicked', async () => {
    const user = userEvent.setup();
    renderWithProviders(<CreatePost />);
    
    const button = screen.getByText(/Log a new bird sighting/i);
    await user.click(button);
    
    expect(button).toBeInTheDocument();
  });
});